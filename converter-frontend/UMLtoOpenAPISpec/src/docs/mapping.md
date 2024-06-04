# Mapping Component

The `MappingComponent` is responsible for mapping UML diagram elements to classes, paths, methods, and attributes that will be included in the OpenAPI specification that the application generates. It includes functionalities to add, modify, and remove mappings and elements, whether they existed in the uploaded UML diagram or they were added during the Mapping step of the stepper.

## Key Features
- Manage and map the classes, attributes, and methods that were included in the uploaded UML diagram.
- Add new classes, assign new paths, attributes, and methods to them, and let them be included in the OpenAPI specification that the application generates.
- Select for each existing or new class HTTP methods for which endpoints will be created in the OpenAPI specification that the application generates.
- Remove or rename any existing or new element (class, attribute or method) and let your changes be reflected in the OpenAPI specification that the application generates.

## Usage

