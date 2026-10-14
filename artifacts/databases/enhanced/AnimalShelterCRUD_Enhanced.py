"""
CS 499 Capstone - Enhancement Three: Databases
Grazioso Salvare Animal Shelter CRUD Module

Enhancements:
- Input validation
- Flexible searching
- Sorting and pagination
- Database indexes
- Improved error handling
"""

import logging
from typing import Any, Dict, List, Optional

from pymongo import ASCENDING, MongoClient
from pymongo.errors import PyMongoError

logger = logging.getLogger(__name__)


class AnimalShelterCRUD:
    """MongoDB access layer for animal shelter records."""

    def __init__(
        self,
        username: str,
        password: str,
        host: str = "localhost",
        port: int = 27017,
        database: str = "aac_demo",
        collection: str = "animals",
        auth_source: str = "admin",
    ) -> None:

        self.client = MongoClient(
    host=host,
    port=port,
    serverSelectionTimeoutMS=5000,
)

        try:
            self.client.admin.command("ping")
            self.database = self.client[database]
            self.collection = self.database[collection]
        except PyMongoError as error:
            self.client.close()
            raise ConnectionError(
                f"MongoDB connection failed: {error}"
            ) from error

    def create(self, data: Dict[str, Any]) -> bool:
        """Insert a validated animal record."""

        if not isinstance(data, dict) or not data:
            return False

        if not self._validate_animal(data):
            return False

        try:
            result = self.collection.insert_one(data.copy())
            return result.acknowledged
        except PyMongoError:
            logger.exception("Failed to create animal record")
            return False

    def _validate_animal(self, data: Dict[str, Any]) -> bool:
        """Validate important animal record fields."""

        for field in ("animal_type", "breed"):
            if field in data:
                value = data[field]
                if not isinstance(value, str) or not value.strip():
                    return False

        if "age_upon_outcome" in data:
            age = data["age_upon_outcome"]
            if not isinstance(age, str) or not age.strip():
                return False

        return True

    def read(
        self,
        query: Optional[Dict[str, Any]] = None,
        sort_by: Optional[str] = None,
        ascending: bool = True,
        page: int = 1,
        page_size: int = 10,
    ) -> List[Dict[str, Any]]:
        """Search records with optional sorting and pagination."""

        if query is None:
            query = {}

        if not isinstance(query, dict):
            return []

        if (
            not isinstance(page, int)
            or isinstance(page, bool)
            or page < 1
            or not isinstance(page_size, int)
            or isinstance(page_size, bool)
            or not 1 <= page_size <= 100
        ):
            return []

        allowed_sort_fields = {
            "animal_type",
            "breed",
            "age_upon_outcome",
            "name",
        }

        if sort_by is not None and sort_by not in allowed_sort_fields:
            return []

        try:
            cursor = self.collection.find(query)

            if sort_by:
                direction = ASCENDING if ascending else -1
                cursor = cursor.sort(
                    [(sort_by, direction), ("_id", ASCENDING)]
                )

            cursor = cursor.skip(
                (page - 1) * page_size
            ).limit(page_size)

            return list(cursor)

        except PyMongoError:
            logger.exception("Failed to read animal records")
            return []

    def search_animals(
        self,
        animal_type: Optional[str] = None,
        breed: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """Search animals using indexed fields."""

        query = {}

        if animal_type:
            query["animal_type"] = animal_type

        if breed:
            query["breed"] = breed

        return self.read(query)

    def update(
        self,
        query: Dict[str, Any],
        update_data: Dict[str, Any],
    ) -> int:
        """Update matching animal records safely."""

        if (
            not isinstance(query, dict)
            or not query
            or not isinstance(update_data, dict)
            or not update_data
        ):
            return 0

        if not self._validate_animal(update_data):
            return 0

        if any(key.startswith("$") or "." in key for key in update_data):
            return 0

        if "_id" in update_data:
            return 0

        try:
            result = self.collection.update_many(
                query, {"$set": update_data}
            )
            return result.modified_count

        except PyMongoError:
            logger.exception("Failed to update animal records")
            return 0

    def delete(self, query: Dict[str, Any]) -> int:
        """Delete matching records; reject empty queries."""

        if not isinstance(query, dict) or not query:
            return 0

        try:
            result = self.collection.delete_many(query)
            return result.deleted_count

        except PyMongoError:
            logger.exception("Failed to delete animal records")
            return 0

    def create_indexes(self) -> List[str]:
        """Ensure indexes exist on frequently searched fields."""

        try:
            animal_index = self.collection.create_index(
                [("animal_type", ASCENDING)]
            )
            breed_index = self.collection.create_index(
                [("breed", ASCENDING)]
            )
            return [animal_index, breed_index]

        except PyMongoError:
            logger.exception("Failed to create indexes")
            return []

    def close(self) -> None:
        """Close the MongoDB connection."""
        self.client.close()
