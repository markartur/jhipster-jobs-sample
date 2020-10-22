import axios from 'axios';
import { ICrudSearchAction, ICrudGetAction, ICrudGetAllAction, ICrudPutAction, ICrudDeleteAction } from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IAccommodation, defaultValue } from 'app/shared/model/accommodation.model';

export const ACTION_TYPES = {
  SEARCH_ACCOMMODATIONS: 'accommodation/SEARCH_ACCOMMODATIONS',
  FETCH_ACCOMMODATION_LIST: 'accommodation/FETCH_ACCOMMODATION_LIST',
  FETCH_ACCOMMODATION: 'accommodation/FETCH_ACCOMMODATION',
  CREATE_ACCOMMODATION: 'accommodation/CREATE_ACCOMMODATION',
  UPDATE_ACCOMMODATION: 'accommodation/UPDATE_ACCOMMODATION',
  DELETE_ACCOMMODATION: 'accommodation/DELETE_ACCOMMODATION',
  RESET: 'accommodation/RESET',
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IAccommodation>,
  entity: defaultValue,
  updating: false,
  updateSuccess: false,
};

export type AccommodationState = Readonly<typeof initialState>;

// Reducer

export default (state: AccommodationState = initialState, action): AccommodationState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.SEARCH_ACCOMMODATIONS):
    case REQUEST(ACTION_TYPES.FETCH_ACCOMMODATION_LIST):
    case REQUEST(ACTION_TYPES.FETCH_ACCOMMODATION):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true,
      };
    case REQUEST(ACTION_TYPES.CREATE_ACCOMMODATION):
    case REQUEST(ACTION_TYPES.UPDATE_ACCOMMODATION):
    case REQUEST(ACTION_TYPES.DELETE_ACCOMMODATION):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true,
      };
    case FAILURE(ACTION_TYPES.SEARCH_ACCOMMODATIONS):
    case FAILURE(ACTION_TYPES.FETCH_ACCOMMODATION_LIST):
    case FAILURE(ACTION_TYPES.FETCH_ACCOMMODATION):
    case FAILURE(ACTION_TYPES.CREATE_ACCOMMODATION):
    case FAILURE(ACTION_TYPES.UPDATE_ACCOMMODATION):
    case FAILURE(ACTION_TYPES.DELETE_ACCOMMODATION):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload,
      };
    case SUCCESS(ACTION_TYPES.SEARCH_ACCOMMODATIONS):
    case SUCCESS(ACTION_TYPES.FETCH_ACCOMMODATION_LIST):
      return {
        ...state,
        loading: false,
        entities: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.FETCH_ACCOMMODATION):
      return {
        ...state,
        loading: false,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.CREATE_ACCOMMODATION):
    case SUCCESS(ACTION_TYPES.UPDATE_ACCOMMODATION):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data,
      };
    case SUCCESS(ACTION_TYPES.DELETE_ACCOMMODATION):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {},
      };
    case ACTION_TYPES.RESET:
      return {
        ...initialState,
      };
    default:
      return state;
  }
};

const apiUrl = 'api/accommodations';
const apiSearchUrl = 'api/_search/accommodations';

// Actions

export const getSearchEntities: ICrudSearchAction<IAccommodation> = (query, page, size, sort) => ({
  type: ACTION_TYPES.SEARCH_ACCOMMODATIONS,
  payload: axios.get<IAccommodation>(`${apiSearchUrl}?query=${query}`),
});

export const getEntities: ICrudGetAllAction<IAccommodation> = (page, size, sort) => ({
  type: ACTION_TYPES.FETCH_ACCOMMODATION_LIST,
  payload: axios.get<IAccommodation>(`${apiUrl}?cacheBuster=${new Date().getTime()}`),
});

export const getEntity: ICrudGetAction<IAccommodation> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_ACCOMMODATION,
    payload: axios.get<IAccommodation>(requestUrl),
  };
};

export const createEntity: ICrudPutAction<IAccommodation> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_ACCOMMODATION,
    payload: axios.post(apiUrl, cleanEntity(entity)),
  });
  dispatch(getEntities());
  return result;
};

export const updateEntity: ICrudPutAction<IAccommodation> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_ACCOMMODATION,
    payload: axios.put(apiUrl, cleanEntity(entity)),
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IAccommodation> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_ACCOMMODATION,
    payload: axios.delete(requestUrl),
  });
  dispatch(getEntities());
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET,
});
