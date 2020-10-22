import { ILocation } from 'app/shared/model/location.model';

export interface IAccommodation {
  id?: string;
  name?: string;
  hotelier?: string;
  category?: string;
  location?: ILocation;
}

export const defaultValue: Readonly<IAccommodation> = {};
