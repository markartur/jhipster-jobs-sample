import { element, by, ElementFinder } from 'protractor';
import { waitUntilDisplayed, waitUntilHidden, isVisible } from '../../util/utils';

const expect = chai.expect;

export default class AccommodationUpdatePage {
  pageTitle: ElementFinder = element(by.id('companyApp.accommodation.home.createOrEditLabel'));
  saveButton: ElementFinder = element(by.id('save-entity'));
  cancelButton: ElementFinder = element(by.id('cancel-save'));
  nameInput: ElementFinder = element(by.css('input#accommodation-name'));
  hotelierInput: ElementFinder = element(by.css('input#accommodation-hotelier'));
  categoryInput: ElementFinder = element(by.css('input#accommodation-category'));
  locationSelect: ElementFinder = element(by.css('select#accommodation-location'));

  getPageTitle() {
    return this.pageTitle;
  }

  async setNameInput(name) {
    await this.nameInput.sendKeys(name);
  }

  async getNameInput() {
    return this.nameInput.getAttribute('value');
  }

  async setHotelierInput(hotelier) {
    await this.hotelierInput.sendKeys(hotelier);
  }

  async getHotelierInput() {
    return this.hotelierInput.getAttribute('value');
  }

  async setCategoryInput(category) {
    await this.categoryInput.sendKeys(category);
  }

  async getCategoryInput() {
    return this.categoryInput.getAttribute('value');
  }

  async locationSelectLastOption() {
    await this.locationSelect.all(by.tagName('option')).last().click();
  }

  async locationSelectOption(option) {
    await this.locationSelect.sendKeys(option);
  }

  getLocationSelect() {
    return this.locationSelect;
  }

  async getLocationSelectedOption() {
    return this.locationSelect.element(by.css('option:checked')).getText();
  }

  async save() {
    await this.saveButton.click();
  }

  async cancel() {
    await this.cancelButton.click();
  }

  getSaveButton() {
    return this.saveButton;
  }

  async enterData() {
    await waitUntilDisplayed(this.saveButton);
    await this.setNameInput('name');
    expect(await this.getNameInput()).to.match(/name/);
    await waitUntilDisplayed(this.saveButton);
    await this.setHotelierInput('hotelier');
    expect(await this.getHotelierInput()).to.match(/hotelier/);
    await waitUntilDisplayed(this.saveButton);
    await this.setCategoryInput('category');
    expect(await this.getCategoryInput()).to.match(/category/);
    await this.locationSelectLastOption();
    await this.save();
    await waitUntilHidden(this.saveButton);
    expect(await isVisible(this.saveButton)).to.be.false;
  }
}
