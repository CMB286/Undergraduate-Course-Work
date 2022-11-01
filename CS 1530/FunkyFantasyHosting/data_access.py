"""This file contains operations for accessing datastore"""
from google.cloud import datastore
from user import User

"""Basic CRUD operations"""
def retrieve_entity(id, kind='user'):
    client = get_client()
    # Make sure this is an int; string will yield a different result.
    # It's OK to use strings instead, but make sure you're consistent.
    key = client.key(kind, int(id))
    return client.get(key)

"""Adds an entity to the datastore"""
def update_entity(entity):
    client = get_client()
    client.put(entity)

"""Deletes a specific entity from the datastore"""
def delete_entity(id, kind='user'):
    client = get_client()
    key = client.key(kind, int(id))
    client.delete(key)

"""Returns a list of entities"""
def get_entities(kind):
    result = []
    client = get_client()
    query = client.query(kind)
    for entity in query.fetch():
        result.append(entity)
    return result

"""Converts a User object into an entity"""
def user_to_entity(user):
    client = get_client()
    key = client.key('user')
    entity = datastore.Entity(key)
    entity['username'] = user.username
    entity['password'] = user.password
    entity['screenname'] = user.screenname
    entity['leagueList'] = user.leagueList
    entity['commishList'] = user.commishList
    entity['avatar'] = user.avatar
    return entity

"""Converts an entity into a User object"""
def entity_to_user(entity):
    username = entity['username']
    password = entity['password']
    screenname = entity['screenname']
    leagueList = entity['leagueList']
    commishList = entity['commishList']
    avatar = entity['avatar']
    user = User(username, password, screenname, leagueList, commishList, avatar)
    return user

"""Takes user object and returns user entity"""
def get_user_entity(user):
    client = get_client()
    query = client.query(kind="user")
    query = query.add_filter("username", "=", user.username)
    # query fetch returns an iterator
    for i in query.fetch():
        # return first (and only) one
        return i

    # query fetch returns an iterator
    for i in query.fetch():
        # return first (and only) one
        return i


"""Returns the current datastore client"""
def get_client():
    return datastore.Client()