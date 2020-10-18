import { ITask } from 'app/shared/model/task.model';
import { IEmployee } from 'app/shared/model/employee.model';

export interface IJob {
  id?: string;
  jobTitle?: string;
  minSalary?: number;
  maxSalary?: number;
  tasks?: ITask[];
  employee?: IEmployee;
}

export const defaultValue: Readonly<IJob> = {};
