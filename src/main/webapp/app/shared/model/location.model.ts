import { ICountry } from 'app/shared/model/country.model';
import { IAccommodation } from 'app/shared/model/accommodation.model';

export interface ILocation {
  id?: string;
  streetAddress?: string;
  postalCode?: string;
  city?: string;
  stateProvince?: string;
  country?: ICountry;
  accommodations?: IAccommodation[];
}

export const defaultValue: Readonly<ILocation> = {};
