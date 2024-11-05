import { Router } from 'express';
import { {{controllerClassName}} } from '../controllers/{{controllerClassName}}';

export class {{routeClassName}} {
    public router: Router;

    constructor() {
        this.router = Router();
        this.initializeRoutes();
    }

    private initializeRoutes() {
        this.router.get('/{{entityName}}s', {{controllerClassName}}.getAll);
        this.router.get('/{{entityName}}s/:id', {{controllerClassName}}.getById);
        this.router.post('/{{entityName}}s', {{controllerClassName}}.create);
        this.router.put('/{{entityName}}s/:id', {{controllerClassName}}.update);
        this.router.delete('/{{entityName}}s/:id', {{controllerClassName}}.delete);
    }
}
