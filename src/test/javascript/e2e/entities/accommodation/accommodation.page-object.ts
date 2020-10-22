import { element, by, ElementFinder, ElementArrayFinder } from 'protractor';

import { waitUntilAnyDisplayed, waitUntilDisplayed, click, waitUntilHidden, isVisible } from '../../util/utils';

import NavBarPage from './../../page-objects/navbar-page';

import AccommodationUpdatePage from './accommodation-update.page-object';

const expect = chai.expect;
export class AccommodationDeleteDialog {
  deleteModal = element(by.className('modal'));
  private dialogTitle: ElementFinder = element(by.id('companyApp.accommodation.delete.question'));
  private confirmButton = element(by.id('jhi-confirm-delete-accommodation'));

  getDialogTitle() {
    return this.dialogTitle;
  }

  async clickOnConfirmButton() {
    await this.confirmButton.click();
  }
}

export default class AccommodationComponentsPage {
  createButton: ElementFinder = element(by.id('jh-create-entity'));
  deleteButtons = element.all(by.css('div table .btn-danger'));
  title: ElementFinder = element(by.id('accommodation-heading'));
  noRecords: ElementFinder = element(by.css('#app-view-container .table-responsive div.alert.alert-warning'));
  table: ElementFinder = element(by.css('#app-view-container div.table-responsive > table'));

  records: ElementArrayFinder = this.table.all(by.css('tbody tr'));

  getDetailsButton(record: ElementFinder) {
    return record.element(by.css('a.btn.btn-info.btn-sm'));
  }

  getEditButton(record: ElementFinder) {
    return record.element(by.css('a.btn.btn-primary.btn-sm'));
  }

  getDeleteButton(record: ElementFinder) {
    return record.element(by.css('a.btn.btn-danger.btn-sm'));
  }

  async goToPage(navBarPage: NavBarPage) {
    await navBarPage.getEntityPage('accommodation');
    await waitUntilAnyDisplayed([this.noRecords, this.table]);
    return this;
  }

  async goToCreateAccommodation() {
    await this.createButton.click();
    return new AccommodationUpdatePage();
  }

  async deleteAccommodation() {
    const deleteButton = this.getDeleteButton(this.records.last());
    await click(deleteButton);

    const accommodationDeleteDialog = new AccommodationDeleteDialog();
    await waitUntilDisplayed(accommodationDeleteDialog.deleteModal);
    expect(await accommodationDeleteDialog.getDialogTitle().getAttribute('id')).to.match(/companyApp.accommodation.delete.question/);
    await accommodationDeleteDialog.clickOnConfirmButton();

    await waitUntilHidden(accommodationDeleteDialog.deleteModal);

    expect(await isVisible(accommodationDeleteDialog.deleteModal)).to.be.false;
  }
}
