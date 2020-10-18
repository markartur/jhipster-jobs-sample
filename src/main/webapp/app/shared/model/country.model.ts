import { IRegion } from 'app/shared/model/region.model';

export interface ICountry {
  id?: string;
  countryName?: string;
  region?: IRegion;
}

export const defaultValue: Readonly<ICountry> = {};
