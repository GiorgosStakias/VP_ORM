package com.ece.vp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TypescriptClassMapper {

    private static final Map<String, String[]> typeMappings = new HashMap<>();

    static {
        typeMappings.put("BIGINT", new String[]{"string", "bigint", "123456789012345"});
        typeMappings.put("BINARY", new String[]{"Buffer", "binary", "AQIDBA=="}); // Base64 encoded example
        typeMappings.put("BIT", new String[]{"boolean", "bit", "true"});
        typeMappings.put("BLOB", new String[]{"Buffer", "blob", "AQIDBA=="});
        typeMappings.put("CHAR", new String[]{"string", "char", "A"});
        typeMappings.put("CLOB", new String[]{"string", "text", "Sample Text"});
        typeMappings.put("DATE", new String[]{"Date", "date", "2024-01-01"});
        typeMappings.put("DATETIME", new String[]{"Date", "datetime", "2024-01-01T10:00:00Z"});
        typeMappings.put("DATETIME2", new String[]{"Date", "datetime2", "2024-01-01T10:00:00.000Z"});
        typeMappings.put("DECIMAL", new String[]{"string", "decimal", "12345.67"});
        typeMappings.put("DOUBLE", new String[]{"number", "double", "12345.67"});
        typeMappings.put("ENUM", new String[]{"string", "enum", "ENUM_VALUE"});
        typeMappings.put("FIXED", new String[]{"string", "decimal", "12345.67"});
        typeMappings.put("FLOAT", new String[]{"number", "float", "12345.67"});
        typeMappings.put("HSTORE", new String[]{"object", "hstore", "{key: value}"});
        typeMappings.put("IMAGE", new String[]{"Buffer", "longblob", "/9j/4AAQSkZJRgABAQEAYABgAAD/"});
        typeMappings.put("INT", new String[]{"number", "int", "123"});
        typeMappings.put("INTEGER", new String[]{"number", "int", "123"});
        typeMappings.put("JSON", new String[]{"object", "json", "{key: value}"});
        typeMappings.put("LONGTEXT", new String[]{"string", "longtext", "This is a long text sample."});
        typeMappings.put("LONGBLOB", new String[]{"Buffer", "longblob", "/9j/4AAQSkZJRgABAQEAYABgAAD/"});
        typeMappings.put("MEDIUMBLOB", new String[]{"Buffer", "mediumblob", "/9j/4AAQSkZJRgABAQEAYABgAAD/"});
        typeMappings.put("MEDIUMINT", new String[]{"number", "mediumint", "12345"});
        typeMappings.put("NCHAR", new String[]{"string", "nchar", "Sample"});
        typeMappings.put("NTEXT", new String[]{"string", "ntext", "Sample Text"});
        typeMappings.put("NUMERIC", new String[]{"string", "numeric", "12345.67"});
        typeMappings.put("NVARCHAR", new String[]{"string", "nvarchar", "Sample"});
        typeMappings.put("REAL", new String[]{"number", "real", "12345.67"});
        typeMappings.put("SMALLDATETIME", new String[]{"Date", "smalldatetime", "2024-01-01T10:00:00"});
        typeMappings.put("SMALLINT", new String[]{"number", "smallint", "123"});
        typeMappings.put("TEXT", new String[]{"string", "text", "Sample Text"});
        typeMappings.put("TIME", new String[]{"Date", "time", "10:00:00"});
        typeMappings.put("TIMESTAMP", new String[]{"Date", "timestamp", "2024-01-01T10:00:00Z"});
        typeMappings.put("TINYBLOB", new String[]{"Buffer", "tinyblob", "AQID"});
        typeMappings.put("TINYINT", new String[]{"number", "tinyint", "1"});
        typeMappings.put("TINYTEXT", new String[]{"string", "tinytext", "Sample"});
        typeMappings.put("UNIQUEIDENTIFIER", new String[]{"string", "uniqueidentifier", "550e8400-e29b-41d4-a716-446655440000"});
        typeMappings.put("UUID", new String[]{"string", "uuid", "550e8400-e29b-41d4-a716-446655440000"});
        typeMappings.put("VARBINARY", new String[]{"Buffer", "varbinary", "AQIDBA=="});
        typeMappings.put("VARCHAR", new String[]{"string", "varchar", "Sample"});
        typeMappings.put("YEAR", new String[]{"number", "year", "2024"});
        typeMappings.put("XML", new String[]{"string", "xml", "<root><child>value</child></root>"});
    }

    public static void generateExpressAppFiles(List<EntityJsonData> entities, String outputPath) {
        try {
            generateAppTs(entities, outputPath);
            generateIndexTs(outputPath);
            generateOrmConfigTs(entities, outputPath);
            generateEnvFile(outputPath);
            System.out.println("Express app files generated successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateAppTs(List<EntityJsonData> entities, String outputPath) throws IOException {
        StringBuilder routeImports = new StringBuilder();
        StringBuilder routeInitializations = new StringBuilder();

        for (EntityJsonData entity : entities) {
            String className = capitalize(entity.getName());
            routeImports.append("import { ").append(toPascalCase(className)).append("Routes } from './routes/").append(toPascalCase(className)).append("Routes';\n");
            routeInitializations.append("const ").append(entity.getName().toLowerCase()).append("Routes = new ").append(toPascalCase(className)).append("Routes();\n");
            routeInitializations.append("app.use('/api/").append(entity.getName().toLowerCase()).append("', ").append(entity.getName().toLowerCase()).append("Routes.router);\n");
        }

        String content = "import 'reflect-metadata';\n"
                + "import dotenv from 'dotenv';\n"
                + "dotenv.config();\n"
                + "import express from 'express';\n"
                + "import { DataSource } from 'typeorm';\n"
                + "import ormconfig from './ormconfig';\n"
                + routeImports
                + "\n"
                + "const app = express();\n"
                + "app.use(express.json());\n\n"
                + "// Configure the TypeORM Data Source\n"
                + "export const AppDataSource = new DataSource(ormconfig);\n\n"
                + "// Initialize routes\n"
                + routeInitializations
                + "\n"
                + "export default app;\n";

        Utils.writeFile(outputPath + "/src/app.ts", content);
    }

    private static void generateIndexTs(String outputPath) throws IOException {
        String content = "import app from './app';\n"
                + "import { AppDataSource } from './app';\n"
                + "import dotenv from 'dotenv';\n"
                + "dotenv.config();\n\n"
                + "AppDataSource.initialize()\n"
                + "  .then(() => {\n"
                + "    const PORT = process.env.PORT || 3000;\n"
                + "    app.listen(PORT, () => {\n"
                + "      console.log(`Server running on port ${PORT}`);\n"
                + "    });\n"
                + "  })\n"
                + "  .catch((error) => {\n"
                + "    console.log('Error during Data Source initialization', error);\n"
                + "  });\n";

        Utils.writeFile(outputPath + "/src/index.ts", content);
    }

    private static void generateOrmConfigTs(List<EntityJsonData> entities, String outputPath) throws IOException {
        StringBuilder entityImports = new StringBuilder();
        StringBuilder entityArray = new StringBuilder();

        for (EntityJsonData entity : entities) {
            String className = toPascalCase(entity.getName());
            entityImports.append("import { ").append(className).append(" } from './entities/").append(entity.getName()).append("';\n");
            entityArray.append(className).append(", ");
        }

        String content = "import { DataSourceOptions } from 'typeorm';\n"
                + "import dotenv from 'dotenv';\n"
                + "dotenv.config();\n"
                + entityImports
                + "\n"
                + "const ormconfig: DataSourceOptions = {\n"
                + "  type: 'mysql',\n"
                + "  host: process.env.DB_HOST,\n"
                + "  port: Number(process.env.DB_PORT),\n"
                + "  username: process.env.DB_USERNAME,\n"
                + "  password: process.env.DB_PASSWORD,\n"
                + "  database: process.env.DB_NAME,\n"
                + "  /* USE IN CASE OF MS SQL */"
                + "  /*options: {\n"
                +"    \n"
                +"    encrypt: false, // Enables encryption\n"
                +"    trustServerCertificate: true, // Allows self-signed certificate\n"
                +"  },\n"
                +"  extra: {\n"
                +"    integratedSecurity: false, // Use for Windows Authentication\n"
                +"    connectionTimeout: 30000,\n"
                +"  },*/\n"
                + "  synchronize: true,\n"
                + "  logging: true,\n"
                + "  entities: [" + entityArray.substring(0, entityArray.length() - 2) + "],\n"
                + "};\n\n"
                + "export default ormconfig;\n";

        Utils.writeFile(outputPath + "/src/ormconfig.ts", content);
    }

    private static void generateEnvFile(String outputPath) throws IOException {
        String content = "# Database configuration\n"
                + "DB_HOST={{DB HOST}}\n"
                + "DB_PORT={{DB PORT}}\n"
                + "DB_USERNAME={{USERNAME}}\n"
                + "DB_PASSWORD={{PASSWORD}}\n"
                + "DB_NAME={{SCHEMA NAME}}\n\n"
                + "# Server configuration\n"
                + "PORT={{EXPRESS SERVER PORT}}\n";

        Utils.writeFile(outputPath + "/.env", content);

        content = "{\n" +
                "    \"watch\": \"src/**/*.ts\",\n" +
                "    \"execMap\": {\n" +
                "      \"ts\": \"ts-node\"\n" +
                "    }\n" +
                "  }";
        Utils.writeFile(outputPath + "/nodemon.json", content);

        content = "{\n" +
                "  \"name\": \"type_ORM_sample\",\n" +
                "  \"version\": \"1.0.0\",\n" +
                "  \"description\": \"\",\n" +
                "  \"main\": \"index.js\",\n" +
                "  \"scripts\": {\n" +
                "    \"test\": \"echo \\\"Error: no test specified\\\" && exit 1\",\n" +
                "    \"dev\": \"nodemon src/index.ts\"\n" +
                "  },\n" +
                "  \"keywords\": [],\n" +
                "  \"author\": \"\",\n" +
                "  \"license\": \"ISC\",\n" +
                "  \"dependencies\": {\n" +
                "    \"@types/express\": \"^4.17.17\",\n" +
                "    \"dotenv\": \"^16.4.5\",\n" +
                "    \"express\": \"^4.18.2\",\n" +
                "    \"mssql\": \"^10.0.4\",\n"+
                "    \"mysql\": \"^2.18.1\",\n" +
                "    \"pg\": \"^8.11.1\",\n" +
                "    \"nodemon\": \"^3.1.7\",\n"+
                "    \"ts-node\": \"^10.9.1\",\n" +
                "    \"typeorm\": \"0.3.20\",\n" +
                "    \"typescript\": \"^5.1.3\"\n" +
                "  }\n" +
                "}\n";
        Utils.writeFile(outputPath + "/package.json", content);

        content = "{\n" +
                "\t\"compilerOptions\": {\n" +
                "\t\t\"target\": \"es6\",\n" +
                "\t\t\"module\": \"commonjs\",\n" +
                "\t\t\"lib\": [\n" +
                "\t\t\t\"dom\",\n" +
                "\t\t\t\"es6\",\n" +
                "\t\t\t\"es2017\",\n" +
                "\t\t\t\"esnext.asynciterable\"\n" +
                "\t\t],\n" +
                "\t\t\"skipLibCheck\": true,\n" +
                "\t\t\"sourceMap\": true,\n" +
                "\t\t\"outDir\": \"./dist\",\n" +
                "\t\t\"moduleResolution\": \"node\",\n" +
                "\t\t\"removeComments\": true,\n" +
                "\t\t\"noImplicitAny\": true,\n" +
                "\t\t\"strictNullChecks\": true,\n" +
                "\t\t\"strictFunctionTypes\": true,\n" +
                "\t\t\"noImplicitThis\": true,\n" +
                "\t\t\"noUnusedLocals\": false,\n" +
                "\t\t\"noUnusedParameters\": false,\n" +
                "\t\t\"noImplicitReturns\": true,\n" +
                "\t\t\"noFallthroughCasesInSwitch\": true,\n" +
                "\t\t\"allowSyntheticDefaultImports\": true,\n" +
                "\t\t\"esModuleInterop\": true,\n" +
                "\t\t\"emitDecoratorMetadata\": true,\n" +
                "\t\t\"experimentalDecorators\": true,\n" +
                "\t\t\"resolveJsonModule\": true,\n" +
                "\t\t\"baseUrl\": \".\"\n" +
                "\t},\n" +
                "\t\"exclude\": [\"node_modules\"],\n" +
                "\t\"include\": [\"./src/**/*.tsx\", \"./src/**/*.ts\"]\n" +
                "}";

        Utils.writeFile(outputPath + "/tsconfig.json", content);
    }

    public static void generateCRUDForEntities(List<EntityJsonData> entities, String projectPath) {
        entities.forEach(entity -> {
            String className = toPascalCase(entity.getName());
            String controllerClassName = className + "Controller";
            String routeClassName = className + "Routes";
            String entityName = entity.getName().toLowerCase();
            String primaryKeyName = "";
            String primaryKeyValue = "id";

            Optional<Attribute> primaryKey = entity.getAttributes().stream().filter(eat -> eat.isPrimary()).findFirst();
            if(primaryKey.isPresent()) {
                primaryKeyName = primaryKey.get().getName();
                String [] pkTypes = getTypeMappings(primaryKey.get().getType());
                if(pkTypes[0].equals("number")) primaryKeyValue = "parseInt(id)";
            }

            try {
                // Read and replace placeholders in controller template
                //String controllerTemplate = new String(Files.readAllBytes(Paths.get("ControllerTemplate.ts")));
                String controllerContent = controllerTemplate
                        .replace("{{className}}", className)
                        .replace("{{entityName}}", entity.getName())
                        .replace("{{controllerClassName}}", controllerClassName)
                        .replace("{{pk}}", primaryKeyName)
                        .replace("{{pkValue}}", primaryKeyValue);

                // Write the controller file
                String controllerFilePath = projectPath + "/src/controllers/" + controllerClassName + ".ts";
                Utils.writeFile(controllerFilePath, controllerContent);

                // Read and replace placeholders in routes template
                //String routesTemplate = new String(Files.readAllBytes(Paths.get("RoutesTemplate.ts")));
                String routesContent = routesTemplate
                        .replace("{{controllerClassName}}", controllerClassName)
                        .replace("{{routeClassName}}", routeClassName)
                        .replace("{{entityName}}", entityName);

                // Write the routes file
                String routeFilePath = projectPath + "/src/routes/" + routeClassName + ".ts";
                Utils.writeFile(routeFilePath, routesContent);

                System.out.println("Generated files for " + className);
            } catch (IOException e) {
                System.err.println("Error generating files for " + className + ": " + e.getMessage());
            }
        });
    }

    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String mapToTypeScriptClass(EntityJsonData entityJsonData) {
        StringBuilder tsClass = new StringBuilder();
        String entityName = entityJsonData.getName();
        List<Attribute> attributes = entityJsonData.getAttributes();
        Set<String> relatedEntities = new HashSet<>();

        System.out.println(entityName + " TS Mapping");

        // Start building the TypeScript class
        tsClass.append("import { Entity, PrimaryColumn, Column, ");

        // Add relationship decorators if any exist
        if (attributesHasRelations(attributes)) {
            tsClass.append("OneToOne, ManyToOne, OneToMany, JoinColumn");
        }

        collectRelatedEntities(attributes, relatedEntities);

        tsClass.append(" } from 'typeorm';\n\n");

        for (String relatedEntity : relatedEntities) {
            tsClass.append("import { ").append(toPascalCase(relatedEntity)).append(" } from './").append(relatedEntity).append("';\n");
        }


        // Add the @Entity decorator and class definition
        tsClass.append("\n@Entity('").append(toPascalCase(entityName).toLowerCase()).append("')\n");
        tsClass.append("export class ").append(toPascalCase(entityName)).append(" {\n\n");

        // Process each attribute and generate the corresponding TypeScript property
        for (Attribute attribute : attributes) {

            String fieldName = attribute.getName();
            String [] fieldTypes = getTypeMappings(attribute.getType());
            boolean isPrimary = attribute.isPrimary();
            boolean isNullable = attribute.isNullable();
            boolean isUnique = attribute.isUnique();
            List<Relation> relations = attribute.getRelations();

            // Handle primary key
            if(relations == null || relations.isEmpty()) {
                if (isPrimary) {
                    tsClass.append("  @PrimaryColumn({ type: '"+fieldTypes[1] +"'})\n");
                } else {
                    tsClass.append("  @Column(");
                    tsClass.append("{ type: '");
                    tsClass.append(fieldTypes[1] +"'");

                    if (isUnique || isNullable) {
                        if (isUnique) tsClass.append(", unique: true");
                        if (isNullable) tsClass.append(", nullable: true ");
                    }
                    tsClass.append("}");
                    tsClass.append(")\n");
                }

                // Add the field definition
                tsClass.append("  ").append(fieldName).append(": ").append(fieldTypes[0]).append(";\n\n");
            }
            // Handle relationships (e.g., ManyToOne)
            else {
                for (Relation relation : relations) {
                    String relationType = relation.getType().name();
                    String relatedEntity = toPascalCase(relation.getRelatedEntity());
                    String referenceColumn = relation.getReferenceColumn();
                    Boolean isOwnerSide = relation.getOwnerSide();

                    if (relationType.equals("ManyToOne")) {
                        tsClass.append("  @ManyToOne(() => ").append(relatedEntity)
                                .append(", (").append(fieldName).append(") => ")
                                .append(fieldName).append(".").append(entityName.toLowerCase()).append(", { nullable: true })\n");
                        tsClass.append("  @JoinColumn({ name: '").append(fieldName).append("', referencedColumnName: '").append(referenceColumn).append("' })\n");
                        tsClass.append("  ").append(fieldName).append(": ").append(relatedEntity).append(";\n\n");
                    }
                    // Handle OneToMany
                    if (relationType.equals("OneToMany")) {
                        tsClass.append("  @OneToMany(() => ").append(relatedEntity)
                                .append(", (").append(fieldName).append(") => ")
                                .append(fieldName).append(".").append(referenceColumn).append(")\n");
                        tsClass.append("  ").append(fieldName).append(": ").append(relatedEntity).append("[];\n\n");
                    }

                    // Handle OneToOne
                    if (relationType.equals("OneToOne")) {
                        tsClass.append("  @OneToOne(() => ").append(relatedEntity)
                                .append(", (").append(fieldName).append(") => ")
                                .append(fieldName).append(".").append(referenceColumn).append(", { nullable: true })\n");
                        if(isOwnerSide) {
                            tsClass.append("  @JoinColumn({ name: '").append(fieldName).append("', referencedColumnName: '").append(referenceColumn).append("' })\n");
                        }
                        tsClass.append("  ").append(fieldName).append(": ").append(relatedEntity).append(";\n\n");
                    }

                }
            }
        }

        tsClass.append("}\n");
        return tsClass.toString();
    }

    // Helper to check if any attribute contains relationships
    private static boolean attributesHasRelations(List<Attribute> attributes) {
        for (Attribute attribute : attributes) {
            List<Relation> relations = attribute.getRelations();
            if (relations != null && !relations.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static void collectRelatedEntities(List<Attribute> attributes, Set<String> relatedEntities) {
        for (Attribute attribute : attributes) {
            List<Relation> relations = attribute.getRelations();
            if (relations != null) {
                for (Relation relation : relations) {
                    relatedEntities.add(relation.getRelatedEntity());
                }
            }
        }
    }

    public static String[] getTypeMappings(String vpType) {
        return typeMappings.getOrDefault(vpType.toUpperCase(), new String[]{"unknown", "unknown", ""});
    }

    // Helper to convert the entity name to PascalCase (e.g., "data_providers" to "DataProviders")
    private static String toPascalCase(String input) {
        StringBuilder result = new StringBuilder();
        for (String word : input.split("_")) {
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase());
        }
        return result.toString();
    }


    public static void generatePostmanCollection(List<EntityJsonData> entities, String diagramName, String projectPath) {
        Map<String, Object> collection = new HashMap<>();

        // Collection info with diagram name
        Map<String, String> info = new HashMap<>();
        info.put("name", diagramName);
        info.put("_postman_id", java.util.UUID.randomUUID().toString());
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        collection.put("info", info);

        // Create variable for base URL
        Map<String, Object> baseUrlVariable = new HashMap<>();
        baseUrlVariable.put("key", "baseUrl");
        baseUrlVariable.put("value", "http://localhost:3000/api");
        baseUrlVariable.put("type", "string");

        List<Map<String, Object>> variableList = new ArrayList<>();
        variableList.add(baseUrlVariable);
        collection.put("variable", variableList);

        // Create folders for each entity
        List<Map<String, Object>> folders = new ArrayList<>();
        for (EntityJsonData entity : entities) {
            String entityName = entity.getName().toLowerCase();
            String className = capitalize(entity.getName());

            // Folder for each entity
            Map<String, Object> folder = new HashMap<>();
            folder.put("name", className);
            List<Map<String, Object>> requests = new ArrayList<>();

            // CRUD requests for the entity
            requests.add(createRequest("Get All " + className, "GET", "/{{baseUrl}}/"+ entityName +"/" + entityName + "s", null));
            requests.add(createRequest("Get " + className + " by ID", "GET", "/{{baseUrl}}/"+ entityName +"/" + entityName + "s/:id", null));
            requests.add(createRequest("Create " + className, "POST", "/{{baseUrl}}/"+ entityName +"/" + entityName + "s", createSampleBody(entity)));
            requests.add(createRequest("Update " + className + " by ID", "PUT", "/{{baseUrl}}/"+ entityName +"/" + entityName + "s/:id", createSampleBody(entity)));
            requests.add(createRequest("Delete " + className + " by ID", "DELETE", "/{{baseUrl}}/"+ entityName +"/" + entityName + "s/:id", null));


            folder.put("item", requests);
            folders.add(folder);
        }

        collection.put("item", folders);

        try {

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Utils.writeFile(projectPath + "/" + diagramName + "_Postman_Collection.json", gson.toJson(collection));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper to create a request with optional sample body
    private static Map<String, Object> createRequest(String name, String method, String url, String sampleBody) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);

        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("method", method);

        Map<String, Object> urlDetails = new HashMap<>();
        urlDetails.put("raw", url);
        urlDetails.put("host", Arrays.asList("{{baseUrl}}"));
        urlDetails.put("path", Arrays.asList(url.replace("/{{baseUrl}}", "").split("/")));

        requestDetails.put("url", urlDetails);

        if (sampleBody != null && (method.equals("POST") || method.equals("PUT"))) {
            Map<String, Object> body = new HashMap<>();
            body.put("mode", "raw");
            body.put("raw", sampleBody);
            requestDetails.put("body", body);

            // Add Content-Type header for JSON
            Map<String, String> jsonHeader = new HashMap<>();
            jsonHeader.put("key", "Content-Type");
            jsonHeader.put("value", "application/json");
            requestDetails.put("header", Collections.singletonList(jsonHeader));
        }

        request.put("request", requestDetails);
        return request;
    }

    // Helper to create a sample JSON body based on entity attributes
    private static String createSampleBody(EntityJsonData entity) {
        Map<String, Object> sampleData = new HashMap<>();

        for (Attribute attribute : entity.getAttributes()) {
            String attributeName = attribute.getName();
            String attributeType = attribute.getType().toUpperCase();

            sampleData.put(attributeName, getTypeMappings(attributeType)[2]);
        }

        try {
            // Convert the sample data map to a JSON string
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(sampleData);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private static String routesTemplate = "import { Router } from 'express';\n" +
            "import { {{controllerClassName}} } from '../controllers/{{controllerClassName}}';\n" +
            "\n" +
            "export class {{routeClassName}} {\n" +
            "    public router: Router;\n" +
            "\n" +
            "    constructor() {\n" +
            "        this.router = Router();\n" +
            "        this.initializeRoutes();\n" +
            "    }\n" +
            "\n" +
            "    private initializeRoutes() {\n" +
            "        this.router.get('/{{entityName}}s', {{controllerClassName}}.getAll);\n" +
            "        this.router.get('/{{entityName}}s/:id', {{controllerClassName}}.getById);\n" +
            "        this.router.post('/{{entityName}}s', {{controllerClassName}}.create);\n" +
            "        this.router.put('/{{entityName}}s/:id', {{controllerClassName}}.update);\n" +
            "        this.router.delete('/{{entityName}}s/:id', {{controllerClassName}}.delete);\n" +
            "    }\n" +
            "}\n";

    private static String controllerTemplate = "import { Request, Response } from 'express';\n" +
            "import { AppDataSource } from '../app';\n" +
            "import { {{className}} } from '../entities/{{entityName}}';\n" +
            "\n" +
            "export class {{controllerClassName}} {\n" +
            "    static async getAll(req: Request, res: Response) {\n" +
            "        const repository = AppDataSource.getRepository({{className}});\n" +
            "        const items = await repository.find();\n" +
            "        res.json(items);\n" +
            "    }\n" +
            "\n" +
            "    static async getById(req: Request, res: Response) {\n" +
            "        const { id } = req.params;\n" +
            "        const repository = AppDataSource.getRepository({{className}});\n" +
            "        const item = await repository.findOneBy({ {{pk}}: {{pkValue}} });\n" +
            "\n" +
            "        if (item) {\n" +
            "            res.json(item);\n" +
            "        } else {\n" +
            "            res.status(404).json({ message: '{{className}} not found' });\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    static async create(req: Request, res: Response) {\n" +
            "        const repository = AppDataSource.getRepository({{className}});\n" +
            "        const newItem = repository.create(req.body);\n" +
            "\n" +
            "        try {\n" +
            "            const savedItem = await repository.save(newItem);\n" +
            "            res.status(201).json(savedItem);\n" +
            "        } catch (error) {\n" +
            "            res.status(400).json({ message: 'Error creating {{className}}', error });\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    static async update(req: Request, res: Response) {\n" +
            "        const { id } = req.params;\n" +
            "        const repository = AppDataSource.getRepository({{className}});\n" +
            "        const item = await repository.findOneBy({ {{pk}}: {{pkValue}} });\n" +
            "\n" +
            "        if (item) {\n" +
            "            repository.merge(item, req.body);\n" +
            "            const updatedItem = await repository.save(item);\n" +
            "            res.json(updatedItem);\n" +
            "        } else {\n" +
            "            res.status(404).json({ message: '{{className}} not found' });\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    static async delete(req: Request, res: Response) {\n" +
            "        const { id } = req.params;\n" +
            "        const repository = AppDataSource.getRepository({{className}});\n" +
            "        const result = await repository.delete({ {{pk}}: {{pkValue}} });\n" +
            "\n" +
            "        if (result.affected) {\n" +
            "            res.status(204).send();\n" +
            "        } else {\n" +
            "            res.status(404).json({ message: '{{className}} not found' });\n" +
            "        }\n" +
            "    }\n" +
            "}\n";


}
