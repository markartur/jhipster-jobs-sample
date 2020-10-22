import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Accommodation from './accommodation';
import AccommodationDetail from './accommodation-detail';
import AccommodationUpdate from './accommodation-update';
import AccommodationDeleteDialog from './accommodation-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={AccommodationUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={AccommodationUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={AccommodationDetail} />
      <ErrorBoundaryRoute path={match.url} component={Accommodation} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={AccommodationDeleteDialog} />
  </>
);

export default Routes;
