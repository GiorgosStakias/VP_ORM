# VP_ORM

# Visual Paradigm TypeScript Back-End Service Plugin

## Overview
This plugin for Visual Paradigm automates the creation of a TypeScript back-end service. It analyzes the active ER diagram to identify entities and relationships and generates a REST API using Express and TypeORM. The generated service is compatible with databases such as MSSQL, MySQL, and PostgreSQL.

## Features
- **Entity Analysis**: Scans the ER diagram for entities and their relationships.
- **TypeScript Code Generation**: Generates TypeORM entities and their mappings.
- **Express API Creation**: Builds RESTful APIs for the entities, including CRUD operations.
- **Database Compatibility**: Tested with MSSQL, MySQL, and PostgreSQL.
- **Customizable Output**: Allows directory selection for saving the generated code.

## Prerequisites
- Visual Paradigm (latest version recommended)
- Node.js (version 16 or later)
- TypeScript
- TypeORM
- Supported database (MSSQL, MySQL, PostgreSQL)

## Installation
1. Clone this repository or download the plugin file.
2. Add the plugin to Visual Paradigm:
   - Navigate to **Tools > Application Options > Plugins**.
   - Click **Add** and select the plugin directory or file.
3. Restart Visual Paradigm.

## Usage
1. Open Visual Paradigm and load your ER diagram.
2. Activate the plugin from the **Plugins** menu.
3. Select the desired output directory for the generated code.
4. The plugin will generate:
   - TypeScript files for each entity and their relationships.
   - RESTful API endpoints using Express.
   - Database configuration using TypeORM.
   - A postman collection containing sample requests for every endpoint exposed by the api.

## Example
Here’s an example structure of the generated project:
```
project/
├── src/
│   ├── controllers/
│   │   ├── DataprovidersController.ts
│   │   ├── ForecastsourcesController.ts
│   │   ├── LocationGenericController.ts
│   │   ├── MeasurementStationController.ts
│   │   ├── MeteoForecastsController.ts
│   │   ├── MeteoObservationsController.ts
│   │   ├── MeteoObservationSourcesController.ts
│   │   ├── MeteoStationOperatorsController.ts
│   │   ├── NGPointsPlaceholderController.ts
│   │   ├── PhysicalQuantitiesController.ts
│   │   ├── PhysicalQuantityAliasesController.ts
│   │   ├── ResolutionCodeController.ts
│   │   ├── RESStationGroupController.ts
│   │   ├── UnitsOfMeasurementAliasesController.ts
│   │   └── UnitsOfMeasurementController.ts
│   ├── entities/
│   │   ├── DataProviders.ts
│   │   ├── ForecastSources.ts
│   │   ├── LocationGeneric.ts
│   │   ├── MeasurementStation.ts
│   │   ├── MeteoForecasts.ts
│   │   ├── MeteoObservations.ts
│   │   ├── MeteoObservationSources.ts
│   │   ├── MeteoStationOperators.ts
│   │   ├── NGPoints_Placeholder.ts
│   │   ├── PhysicalQuantities.ts
│   │   ├── PhysicalQuantityAliases.ts
│   │   ├── ResolutionCode.ts
│   │   ├── RESStationGroup.ts
│   │   ├── UnitsOfMeasurement.ts
│   │   └── UnitsOfMeasurementAliases.ts
│   ├── routes/
│   │   ├── DataprovidersRoutes.ts
│   │   ├── ForecastsourcesRoutes.ts
│   │   ├── LocationGenericRoutes.ts
│   │   ├── MeasurementStationRoutes.ts
│   │   ├── MeteoForecastsRoutes.ts
│   │   ├── MeteoObservationsRoutes.ts
│   │   ├── MeteoObservationSourcesRoutes.ts
│   │   ├── MeteoStationOperatorsRoutes.ts
│   │   ├── NGPointsPlaceholderRoutes.ts
│   │   ├── PhysicalQuantitiesRoutes.ts
│   │   ├── PhysicalQuantityAliasesRoutes.ts
│   │   ├── ResolutionCodeRoutes.ts
│   │   ├── RESStationGroupRoutes.ts
│   │   ├── UnitsOfMeasurementAliasesRoutes.ts
│   │   └── UnitsOfMeasurementRoutes.ts
│   ├── app.ts
│   ├── index.ts
│   ├── ormconfig.ts
│   └── typeMapper.ts
├── .env
├── Common - MeteoData_Postman_Collection.json
├── nodemon.json
├── package.json
└── tsconfig.json
```

## Configuration
To configure the API, create a `.env` file in the root directory with the following structure:

```plaintext
# Database configuration
DB_TYPE={{DB TYPE}}
DB_HOST={{DB HOST}}
DB_PORT={{DB PORT}}
DB_USERNAME={{USERNAME}}
DB_PASSWORD={{PASSWORD}}
DB_NAME={{SCHEMA NAME}}

# Server configuration
PORT={{EXPRESS SERVER PORT}}
```

### Example `.env` file
```plaintext
# Database configuration
DB_TYPE=mysql
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=your_password
DB_NAME=your_database

# Server configuration
PORT=3000
```

The `ormconfig.ts` file reads these environment variables to configure the database connection:

```typescript
const ormconfig: DataSourceOptions = {
  type: process.env.DB_TYPE as 'mysql' | 'postgres' | 'mssql',
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT),
  username: process.env.DB_USERNAME,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  synchronize: true, // Automatically syncs database schema
  logging: true,     // Enables logging
  entities: [
    Resstationgroup,
    Resolutioncode,
    Measurementstation,
    NgpointsPlaceholder,
    // Add other entity classes here
  ],
  // Additional options for specific databases
  /* Use in case of MS SQL */
  /*
  options: {
    encrypt: false, // Enables encryption
    trustServerCertificate: true, // Allows self-signed certificates
  },
  extra: {
    integratedSecurity: false, // Use for Windows Authentication
    connectionTimeout: 30000,
  },
  */
};

export default ormconfig;
```

The `PORT` variable from the `.env` file is used to configure the Express server's listening port.

## Development
To test the plugin:
1. Load a sample ER diagram in Visual Paradigm.
2. Run the plugin and inspect the generated code.
3. Deploy the code locally using:
   ```bash
   npm install
   npm run dev
   ```
4. Access the API at `http://localhost:3000`.
5. Import the postman collection and test the api.
