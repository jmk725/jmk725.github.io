"""
CRUD Python Module for CS 340 Module Four Milestone
Provides Create and Read operations for MongoDB using PyMongo.

Database: aac
Collection: animals
User: aacuser (created with role readWrite on db aac)
"""

from typing import Any, Dict, List, Optional
from pymongo import MongoClient
from pymongo.collection import Collection
from pymongo.errors import PyMongoError


class AnimalShelterCRUD:
    """
    CRUD access layer for the AAC animals collection.
    """

    def __init__(
        self,
        username: str,
        password: str,
        host: str = "localhost",
        port: int = 27017,
        database: str = "aac",
        collection: str = "animals",
        auth_source: str = "admin",
    ) -> None:
        """
        Initialize MongoDB connection using authentication.
        """
        self.username = username
        self.password = password
        self.host = host
        self.port = port
        self.database_name = database
        self.collection_name = collection
        self.auth_source = auth_source

        # Build a MongoDB URI with authSource (important when user is created in admin)
        uri = (
            f"mongodb://{self.username}:{self.password}"
            f"@{self.host}:{self.port}/{self.database_name}"
            f"?authSource={self.auth_source}"
        )

        try:
            self.client: MongoClient = MongoClient(uri)
            self.database = self.client[self.database_name]
            self.collection: Collection = self.database[self.collection_name]

            # Quick connectivity/auth check (forces a round-trip)
            self.client.admin.command("ping")

        except PyMongoError as e:
            # Raise a clear error so the notebook shows what failed
            raise ConnectionError(f"MongoDB connection/auth failed: {e}") from e

    def create(self, data: Dict[str, Any]) -> bool:
        """
        Insert a document into the collection.

        Args:
            data: dict of key/value pairs acceptable to insert_one()

        Returns:
            True if insert succeeds, else False
        """
        if not isinstance(data, dict) or len(data) == 0:
            return False

        try:
            result = self.collection.insert_one(data)
            return result.acknowledged
        except PyMongoError:
            return False

    def read(self, query: Optional[Dict[str, Any]] = None) -> List[Dict[str, Any]]:
        """
        Query for documents from the collection using find().

        Args:
            query: dict of key/value pairs for find()

        Returns:
            List of documents if successful, else empty list
        """
        if query is None:
            query = {}

        if not isinstance(query, dict):
            return []

        try:
            cursor = self.collection.find(query)  # IMPORTANT: find(), not find_one()
            return list(cursor)  # Convert cursor to list
        except PyMongoError:
            return []

    def update(self, query: Dict[str, Any], update_data: Dict[str, Any]) -> int:
        """
        Updates document(s) in the collection.
        Returns: The number of objects modified in the collection.
        """
        if query is not None and update_data is not None:
            try:
                # Uses update_many to support plural "document(s)" requirement
                result = self.collection.update_many(query, {"$set": update_data})
                return result.modified_count
            except PyMongoError:
                return 0
        else:
            return 0

    def delete(self, query: Dict[str, Any]) -> int:
        """
        Removes document(s) from the collection.
        Returns: The number of objects removed from the collection.
        """
        if query is not None:
            try:
                # Uses delete_many to support plural "document(s)" requirement
                result = self.collection.delete_many(query)
                return result.deleted_count
            except PyMongoError:
                return 0
        else:
            return 0