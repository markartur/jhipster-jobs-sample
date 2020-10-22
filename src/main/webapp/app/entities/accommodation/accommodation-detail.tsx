import React, { useEffect } from 'react';
import { connect } from 'react-redux';
import { Link, RouteComponentProps } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate, ICrudGetAction } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { IRootState } from 'app/shared/reducers';
import { getEntity } from './accommodation.reducer';
import { IAccommodation } from 'app/shared/model/accommodation.model';
import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';

export interface IAccommodationDetailProps extends StateProps, DispatchProps, RouteComponentProps<{ id: string }> {}

export const AccommodationDetail = (props: IAccommodationDetailProps) => {
  useEffect(() => {
    props.getEntity(props.match.params.id);
  }, []);

  const { accommodationEntity } = props;
  return (
    <Row>
      <Col md="8">
        <h2>
          <Translate contentKey="companyApp.accommodation.detail.title">Accommodation</Translate> [<b>{accommodationEntity.id}</b>]
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="name">
              <Translate contentKey="companyApp.accommodation.name">Name</Translate>
            </span>
          </dt>
          <dd>{accommodationEntity.name}</dd>
          <dt>
            <span id="hotelier">
              <Translate contentKey="companyApp.accommodation.hotelier">Hotelier</Translate>
            </span>
          </dt>
          <dd>{accommodationEntity.hotelier}</dd>
          <dt>
            <span id="category">
              <Translate contentKey="companyApp.accommodation.category">Category</Translate>
            </span>
          </dt>
          <dd>{accommodationEntity.category}</dd>
          <dt>
            <Translate contentKey="companyApp.accommodation.location">Location</Translate>
          </dt>
          <dd>{accommodationEntity.location ? accommodationEntity.location.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/accommodation" replace color="info">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/accommodation/${accommodationEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

const mapStateToProps = ({ accommodation }: IRootState) => ({
  accommodationEntity: accommodation.entity,
});

const mapDispatchToProps = { getEntity };

type StateProps = ReturnType<typeof mapStateToProps>;
type DispatchProps = typeof mapDispatchToProps;

export default connect(mapStateToProps, mapDispatchToProps)(AccommodationDetail);
