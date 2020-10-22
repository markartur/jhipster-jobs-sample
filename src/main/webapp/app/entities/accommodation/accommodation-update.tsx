import React, { useState, useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col, Label } from 'reactstrap';
import { AvFeedback, AvForm, AvGroup, AvInput, AvField } from 'availity-reactstrap-validation';
import { Translate, translate, ICrudGetAction, ICrudGetAllAction, ICrudPutAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IRootState } from 'app/shared/reducers';

import { getEntity, updateEntity, createEntity, reset } from './accommodation.reducer';
import { IAccommodation } from 'app/shared/model/accommodation.model';
import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';

export interface IAccommodationUpdateProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const AccommodationUpdate = (props: IAccommodationUpdateProps) => {
  const [isNew, setIsNew] = useState(!props.match.params || !props.match.params.id);

  const { accommodationEntity, loading, updating } = props;

  const handleClose = () => {
    props.history.push('/accommodation');
  };

  useEffect(() => {
    if (isNew) {
      props.reset();
    } else {
      props.getEntity(props.match.params.id);
    }
  }, []);

  useEffect(() => {
    if (props.updateSuccess) {
      handleClose();
    }
  }, [props.updateSuccess]);

  const saveEntity = (event, errors, values) => {
    if (errors.length === 0) {
      const entity = {
        ...accommodationEntity,
        ...values,
      };

      if (isNew) {
        props.createEntity(entity);
      } else {
        props.updateEntity(entity);
      }
    }
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="companyApp.accommodation.home.createOrEditLabel">
            <Translate contentKey="companyApp.accommodation.home.createOrEditLabel">Create or edit a Accommodation</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <AvForm model={isNew ? {} : accommodationEntity} onSubmit={saveEntity}>
              {!isNew ? (
                <AvGroup>
                  <Label for="accommodation-id">
                    <Translate contentKey="global.field.id">ID</Translate>
                  </Label>
                  <AvInput id="accommodation-id" type="text" className="form-control" name="id" required readOnly />
                </AvGroup>
              ) : null}
              <AvGroup>
                <Label id="nameLabel" for="accommodation-name">
                  <Translate contentKey="companyApp.accommodation.name">Name</Translate>
                </Label>
                <AvField id="accommodation-name" type="text" name="name" />
              </AvGroup>
              <AvGroup>
                <Label id="hotelierLabel" for="accommodation-hotelier">
                  <Translate contentKey="companyApp.accommodation.hotelier">Hotelier</Translate>
                </Label>
                <AvField id="accommodation-hotelier" type="text" name="hotelier" />
              </AvGroup>
              <AvGroup>
                <Label id="categoryLabel" for="accommodation-category">
                  <Translate contentKey="companyApp.accommodation.category">Category</Translate>
                </Label>
                <AvField id="accommodation-category" type="text" name="category" />
              </AvGroup>
              <Button tag={Link} id="cancel-save" to="/accommodation" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </AvForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

const mapStateToProps = (storeState: IRootState) => ({
  accommodationEntity: storeState.accommodation.entity,
  loading: storeState.accommodation.loading,
  updating: storeState.accommodation.updating,
  updateSuccess: storeState.accommodation.updateSuccess,
});

const mapDispatchToProps = {
  getEntity,
  updateEntity,
  createEntity,
  reset,
};

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(AccommodationUpdate);
