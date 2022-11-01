#include <linux/syscalls.h>
#include <linux/kernel.h>
#include <linux/uaccess.h>
#include <linux/spinlock.h>
#include <linux/stddef.h>
#include <linux/list.h>
#include <linux/slab.h>
#include <linux/cs1550.h>


static LIST_HEAD(sem_list);
static int id = 0;
static DEFINE_RWLOCK(sem_list_lock);

/**
 * Creates a new semaphore. The long integer value is used to
 * initialize the semaphore's value.
 *
 * The initial `value` must be greater than or equal to zero.
 *
 * On success, returns the identifier of the created
 * semaphore, which can be used with up() and down().
 *
 * On failure, returns -EINVAL or -ENOMEM, depending on the
 * failure condition.
 */
SYSCALL_DEFINE1(cs1550_create, long, value)
{
	//Allocate and init semaphore
	struct cs1550_sem *sem = kmalloc(sizeof(struct cs1550_sem), GFP_ATOMIC);

	if(sem == NULL){
		return -ENOMEM;
	}

	if(value < 0){
		return -EINVAL;
	}

	sem->value = value;
	sem->sem_id = id;
	spin_lock_init(&sem->lock);
	INIT_LIST_HEAD(&sem->list);
	INIT_LIST_HEAD(&sem->waiting_tasks);

	//Insert into sem list and increment global id count
	write_lock(&sem_list_lock);
	list_add(&sem->list, &sem_list);
	id++;
	write_unlock(&sem_list_lock);

	return sem->sem_id;
}

/**
 * Performs the down() operation on an existing semaphore
 * using the semaphore identifier obtained from a previous call
 * to cs1550_create().
 *
 * This decrements the value of the semaphore, and *may cause* the
 * calling process to sleep (if the semaphore's value goes below 0)
 * until up() is called on the semaphore by another process.
 *
 * Returns 0 when successful, or -EINVAL or -ENOMEM if an error
 * occurred.
 */
SYSCALL_DEFINE1(cs1550_down, long, sem_id)
{
	read_lock(&sem_list_lock);

	//Search for semaphore
	struct cs1550_sem *sem = NULL;
	list_for_each_entry(sem, &sem_list, list) {
		if(sem_id == sem->sem_id){
			break;
		}
	}

	if(sem == NULL){
		read_unlock(&sem_list_lock);
		return -EINVAL;
	}

	spin_lock(&sem->lock);
	sem->value--;
	if(sem->value < 0){
		//Allocate, initialize, and insert task entry to queue of waiting tasks
		struct cs1550_task *task_node = kmalloc(sizeof(struct cs1550_task), GFP_ATOMIC);
		INIT_LIST_HEAD(&task_node->list);
		list_add_tail(&task_node->list, &sem->waiting_tasks);
		task_node->task = current;

		if(task_node == NULL){
			spin_unlock(&sem->lock);
			read_unlock(&sem_list_lock);
			return -EINVAL;
		}

		set_current_state(TASK_INTERRUPTIBLE);		//Process state to sleep
		spin_unlock(&sem->lock);
		schedule();		//Call scheduler
	}
	else{
		spin_unlock(&sem->lock);
	}
	read_unlock(&sem_list_lock);

	return 0;
}

/**
 * Performs the up() operation on an existing semaphore
 * using the semaphore identifier obtained from a previous call
 * to cs1550_create().
 *
 * This increments the value of the semaphore, and *may cause* the
 * calling process to wake up a process waiting on the semaphore,
 * if such a process exists in the queue.
 *
 * Returns 0 when successful, or -EINVAL if the semaphore ID is
 * invalid.
 */
SYSCALL_DEFINE1(cs1550_up, long, sem_id)
{
	read_lock(&sem_list_lock);

	//Search for given semaphor
	struct cs1550_sem *sem = NULL;
	list_for_each_entry(sem, &sem_list, list) {
		if(sem_id == sem->sem_id){
			break;
		}
	}

	if(sem == NULL){
		read_unlock(&sem_list_lock);
		return -EINVAL;
	}


	spin_lock(&sem->lock);
	sem->value++;
	if(sem->value <= 0){
		//Check list not empty
		if (!list_empty(&sem->waiting_tasks)){
			//Remove list head item
			struct cs1550_task *task_node = list_first_entry(&sem->waiting_tasks, struct cs1550_task, list);

			//Remove task, wake up, and free list
			list_del(&task_node->list);
			wake_up_process(task_node->task);	//Wake up process
			kfree(&task_node->list);
		}
	}	
	spin_unlock(&sem->lock);
	read_unlock(&sem_list_lock);
	return 0;
}

/**
 * Removes an already-created semaphore from the system-wide
 * semaphore list using the identifier obtained from a previous
 * call to cs1550_create().
 *
 * Returns 0 when successful or -EINVAL if the semaphore ID is
 * invalid or the semaphore's process queue is not empty.
 */
SYSCALL_DEFINE1(cs1550_close, long, sem_id)
{
	write_lock(&sem_list_lock);

	//Search for semaphore
	struct cs1550_sem *sem = NULL;
	list_for_each_entry(sem, &sem_list, list) {
		if(sem_id == sem->sem_id){
			break;
		}
	} 
	
	if(sem == NULL){
		write_unlock(&sem_list_lock);
		return -EINVAL;
	}

	spin_lock(&sem->lock);
	if(list_empty(&sem->waiting_tasks)){
		//When list empty, remove semaphore because it doesnt have any more tasks
		list_del(&sem->list);
		spin_unlock(&sem->lock);
		kfree(sem);
	}
	else{
		spin_unlock(&sem->lock);
		return -EINVAL;
	}
	
	write_unlock(&sem_list_lock);

	return 0;
}
