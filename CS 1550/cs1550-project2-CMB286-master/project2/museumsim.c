#include <pthread.h>
#include <stdlib.h>
#include <stdio.h>

#include "museumsim.h"

//
// In all of the definitions below, some code has been provided as an example
// to get you started, but you do not have to use it. You may change anything
// in this file except the function signatures.
//

#define VISITORS_PER_GUIDE 10
#define GUIDES_ALLOWED_INSIDE 2


struct shared_data {
		pthread_mutex_t mutex;
		pthread_barrier_t guideBarrier;
		pthread_cond_t numberOfGuides, guideAdmit, visitorAdmit, guideLeave;
	    int ticketsAvail, guidesInside, visitorsWaiting, visitorsInside, readyToLeave, admitFlag, ticketsClaimed;
};

static struct shared_data shared;


/**
 * Set up the shared variables for your implementation.
 * 
 * `museum_init` will be called before any threads of the simulation
 * are spawned.
 */
void museum_init(int num_guides, int num_visitors)
{
	pthread_mutex_init(&shared.mutex, NULL);

	pthread_barrier_init(&shared.guideBarrier, NULL, 2);

	pthread_cond_init(&shared.numberOfGuides, NULL);
	pthread_cond_init(&shared.guideAdmit, NULL);	
	pthread_cond_init(&shared.visitorAdmit, NULL);	
	pthread_cond_init(&shared.guideLeave, NULL);

	shared.ticketsClaimed = 0;
	shared.admitFlag = 1;
	shared.readyToLeave = 0;
	shared.guidesInside = 0;
	shared.visitorsWaiting = 0;
	shared.visitorsInside = 0;
	shared.ticketsAvail = MIN(VISITORS_PER_GUIDE * num_guides, num_visitors);
}


/**
 * Tear down the shared variables for your implementation.
 * 
 * `museum_destroy` will be called after all threads of the simulation
 * are done executing.
 */
void museum_destroy()
{
	pthread_mutex_destroy(&shared.mutex);

	pthread_barrier_destroy(&shared.guideBarrier);

	pthread_cond_destroy(&shared.numberOfGuides);
	pthread_cond_destroy(&shared.guideAdmit);
	pthread_cond_destroy(&shared.visitorAdmit);
	pthread_cond_destroy(&shared.guideLeave);
}


/**
 * Implements the visitor arrival, touring, and leaving sequence.
 */
void visitor(int id)
{
	visitor_arrives(id);

	//Acquire lock
	pthread_mutex_lock(&shared.mutex);

		//If ticketsAvail are all claimed, visitor leaves
		if(shared.ticketsAvail == shared.ticketsClaimed){
			visitor_leaves(id);
			pthread_mutex_unlock(&shared.mutex);
			return;
		}
	
		//Take ticket and add that we have a waiting visitor
		shared.ticketsClaimed += 1;
		shared.visitorsWaiting += 1;


		//Broadcast that the user is in queue
		pthread_cond_broadcast(&shared.guideAdmit);

		//Wait until we have been admitted
		pthread_cond_wait(&shared.visitorAdmit, &shared.mutex);

	
	//Once the visitor thread has entered the museum, it should indicate that it is touring by calling the visitor_tours
	pthread_mutex_unlock(&shared.mutex);
		visitor_tours(id);
	pthread_mutex_lock(&shared.mutex);

		//Once a visitor thread is done touring, it should indicate that it is leaving by calling visitor_leaves
		//Broadcast to any guides waiting for the museum to be empty to leave
		visitor_leaves(id);
		shared.visitorsInside -= 1;
		pthread_cond_broadcast(&shared.guideLeave);

	pthread_mutex_unlock(&shared.mutex);
}

/**
 * Implements the guide arrival, entering, admitting, and leaving sequence.
 */
void guide(int id)
{	
	guide_arrives(id);
	//Acquire lock
	pthread_mutex_lock(&shared.mutex);

		//If there is no one else to admit, leave
		if(shared.ticketsClaimed == shared.ticketsAvail && shared.visitorsWaiting == 0){
			guide_leaves(id);
			pthread_mutex_unlock(&shared.mutex);
			return;
		}

		//While guidesInside is equal to maximum number of guides or guides are waiting to leave, we have to wait
		while(shared.guidesInside == GUIDES_ALLOWED_INSIDE || shared.readyToLeave > 0){
			pthread_cond_wait(&shared.numberOfGuides, &shared.mutex);
		}

		//Can enter museum when there is open spot
		guide_enters(id);
		shared.guidesInside += 1;
		int visitorsServed = 0;

		//Keep admitting visitors until guide has reached the limit and there are still visitors to admit
		while(visitorsServed < VISITORS_PER_GUIDE && shared.admitFlag){
			//If we are out of tickets and the queue is empty, break out of the loop
			if(shared.ticketsClaimed == shared.ticketsAvail && shared.visitorsWaiting == 0){
				shared.admitFlag = 0;
				break;
			}
			shared.admitFlag = 1;

			//While there are no visitors in line or we are at max capacity, should wait for more visitors
			while(shared.visitorsWaiting == 0 || shared.visitorsInside == shared.guidesInside * VISITORS_PER_GUIDE){
				pthread_cond_wait(&shared.guideAdmit, &shared.mutex);

				//If all the tickets are claimed, break out of the loop
				if(shared.ticketsAvail == shared.ticketsClaimed){
					shared.admitFlag = 1;
					break;
				}
			}

			//If we have a visitor in line, admit them and adjust variables
			if(shared.admitFlag && shared.visitorsWaiting > 0){
				guide_admits(id);
				shared.visitorsWaiting -= 1;
				shared.visitorsInside += 1;
				visitorsServed += 1;

				pthread_cond_signal(&shared.visitorAdmit);
			}
		}

		shared.readyToLeave += 1;
		//Broadcast to any other waiting guides
		pthread_cond_broadcast(&shared.guideLeave);


		//While we still have visitors inside and still have guides inside, have to wait
		while(shared.visitorsInside > 0 || shared.guidesInside > shared.readyToLeave){
			pthread_cond_wait(&shared.guideLeave, &shared.mutex);
		}

		//Use a barrier to sync other guide threads together
		if(shared.guidesInside == 2){
			pthread_mutex_unlock(&shared.mutex);
			pthread_barrier_wait(&shared.guideBarrier);
			pthread_mutex_lock(&shared.mutex);
		}

		guide_leaves(id);
		shared.guidesInside -= 1;
		shared.readyToLeave -= 1;
	
		//The last guide to exit sets guidesInside to 0, then signals other guides they can enter
		if(shared.guidesInside == 0){
			pthread_cond_broadcast(&shared.numberOfGuides);
		}

	pthread_mutex_unlock(&shared.mutex);
}
