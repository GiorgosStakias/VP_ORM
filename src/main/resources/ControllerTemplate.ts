import { Request, Response } from 'express';
import { AppDataSource } from '../server';
import { {{className}} } from '../entities/{{className}}';

export class {{controllerClassName}} {
    static async getAll(req: Request, res: Response) {
        const repository = AppDataSource.getRepository({{className}});
        const items = await repository.find();
        res.json(items);
    }

    static async getById(req: Request, res: Response) {
        const { id } = req.params;
        const repository = AppDataSource.getRepository({{className}});
        const item = await repository.findOneBy({ id: parseInt(id) });

        if (item) {
            res.json(item);
        } else {
            res.status(404).json({ message: '{{className}} not found' });
        }
    }

    static async create(req: Request, res: Response) {
        const repository = AppDataSource.getRepository({{className}});
        const newItem = repository.create(req.body);

        try {
            const savedItem = await repository.save(newItem);
            res.status(201).json(savedItem);
        } catch (error) {
            res.status(400).json({ message: 'Error creating {{className}}', error });
        }
    }

    static async update(req: Request, res: Response) {
        const { id } = req.params;
        const repository = AppDataSource.getRepository({{className}});
        const item = await repository.findOneBy({ id: parseInt(id) });

        if (item) {
            repository.merge(item, req.body);
            const updatedItem = await repository.save(item);
            res.json(updatedItem);
        } else {
            res.status(404).json({ message: '{{className}} not found' });
        }
    }

    static async delete(req: Request, res: Response) {
        const { id } = req.params;
        const repository = AppDataSource.getRepository({{className}});
        const result = await repository.delete({ id: parseInt(id) });

        if (result.affected) {
            res.status(204).send();
        } else {
            res.status(404).json({ message: '{{className}} not found' });
        }
    }
}
