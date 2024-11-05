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
                + "  synchronize: true,\n"
                + "  logging: true,\n"
                + "  entities: [" + entityArray.substring(0, entityArray.length() - 2) + "],\n"
                + "};\n\n"
                + "export default ormconfig;\n";

        Utils.writeFile(outputPath + "/src/ormconfig.ts", content);
    }

    private static void generateEnvFile(String outputPath) throws IOException {
        String content = "# Database configuration\n"
                + "DB_HOST=localhost\n"
                + "DB_PORT=3306\n"
                + "DB_USERNAME=root\n"
                + "DB_PASSWORD=password\n"
                + "DB_NAME=meteodata\n\n"
                + "# Server configuration\n"
                + "PORT=8080\n";

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
                "    \"mysql\": \"^2.18.1\",\n" +
                "    \"pg\": \"^8.11.1\",\n" +
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
            Optional<Attribute> primaryKey = entity.getAttributes().stream().filter(eat -> eat.isPrimary()).findFirst();
            if(primaryKey.isPresent())
                primaryKeyName = primaryKey.get().getName();

            try {
                // Read and replace placeholders in controller template
                //String controllerTemplate = new String(Files.readAllBytes(Paths.get("ControllerTemplate.ts")));
                String controllerContent = controllerTemplate
                        .replace("{{className}}", className)
                        .replace("{{entityName}}", entity.getName())
                        .replace("{{controllerClassName}}", controllerClassName)
                        .replace("{{pk}}", primaryKeyName);

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
            tsClass.append("ManyToOne, OneToMany, JoinColumn");
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
            String fieldType = getTypescriptType(attribute.getType());
            boolean isPrimary = attribute.isPrimary();
            boolean isNullable = attribute.isNullable();
            boolean isUnique = attribute.isUnique();
            List<Relation> relations = attribute.getRelations();

            // Handle primary key
            if(relations == null || relations.isEmpty()) {
                if (isPrimary) {
                    tsClass.append("  @PrimaryColumn()\n");
                } else {
                    tsClass.append("  @Column(");
                    if (isUnique || isNullable) {
                        tsClass.append("{ ");
                        if (isUnique) tsClass.append("unique: true, ");
                        if (isNullable) tsClass.append("nullable: true ");
                        tsClass.append("}");
                    }
                    tsClass.append(")\n");
                }

                // Add the field definition
                tsClass.append("  ").append(fieldName).append(": ").append(fieldType).append(";\n\n");
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

    // Helper to convert the provided type (e.g., "DBColumn | varchar") into TypeScript types
    private static String getTypescriptType(String dbType) {
        if (dbType.contains("bigint") || dbType.contains("int")
                || dbType.contains("float") || dbType.contains("double")
                || dbType.contains("bit")) {
            return "number";
        } else if (dbType.contains("varchar")) {
            return "string";
        } else if (dbType.contains("timestamp") || dbType.contains("datetime")) {
            return "Date";
        }
        return "any"; // Fallback type
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
            requests.add(createRequest("Get All " + className, "GET", "/{{baseUrl}}/" + entityName + "s", null));
            requests.add(createRequest("Get " + className + " by ID", "GET", "/{{baseUrl}}/" + entityName + "s/:id", null));
            requests.add(createRequest("Create " + className, "POST", "/{{baseUrl}}/" + entityName + "s", createSampleBody(entity)));
            requests.add(createRequest("Update " + className + " by ID", "PUT", "/{{baseUrl}}/" + entityName + "s/:id", createSampleBody(entity)));
            requests.add(createRequest("Delete " + className + " by ID", "DELETE", "/{{baseUrl}}/" + entityName + "s/:id", null));


            folder.put("item", requests);
            folders.add(folder);
        }

        collection.put("item", folders);

        try {

            //Writer writer = new FileWriter("D:\\Giorgos_Stakias\\SHMMY\\0_Diploma\\DiagramsJson\\"+diagramName + "_Postman_Collection.json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Utils.writeFile("/" + projectPath+diagramName + "_Postman_Collection.json", gson.toJson(collection));

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
            String attributeType = attribute.getType().toLowerCase();

            // Generate sample values based on attribute type
            if (attributeType.contains("string")||attributeType.contains("varchar")) {
                sampleData.put(attributeName, "sample value");
            } else if (attributeType.contains("bigint") || attributeType.contains("int")
                    || attributeType.contains("float") || attributeType.contains("double")
                    || attributeType.contains("bit") || attributeType.contains("number")) {
                sampleData.put(attributeName, 123);
            } else if (attributeType.contains("date")||attributeType.contains("timestamp") || attributeType.contains("datetime")) {
                sampleData.put(attributeName, "0001-01-01T00:00:00Z");
            } else if (attributeType.contains("boolean")) {
                sampleData.put(attributeName, true);
            } else {
                sampleData.put(attributeName, null); // Default to null for unsupported types
            }
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
            "        const item = await repository.findOneBy({ {{pk}}: parseInt(id) });\n" +
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
            "        const item = await repository.findOneBy({ {{pk}}: parseInt(id) });\n" +
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
            "        const result = await repository.delete({ {{pk}}: parseInt(id) });\n" +
            "\n" +
            "        if (result.affected) {\n" +
            "            res.status(204).send();\n" +
            "        } else {\n" +
            "            res.status(404).json({ message: '{{className}} not found' });\n" +
            "        }\n" +
            "    }\n" +
            "}\n";


}
