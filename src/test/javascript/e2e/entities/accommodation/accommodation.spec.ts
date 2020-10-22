import { browser, element, by } from 'protractor';

import NavBarPage from './../../page-objects/navbar-page';
import SignInPage from './../../page-objects/signin-page';
import AccommodationComponentsPage from './accommodation.page-object';
import AccommodationUpdatePage from './accommodation-update.page-object';
import {
  waitUntilDisplayed,
  waitUntilAnyDisplayed,
  click,
  getRecordsCount,
  waitUntilHidden,
  waitUntilCount,
  isVisible,
} from '../../util/utils';

const expect = chai.expect;

describe('Accommodation e2e test', () => {
  let navBarPage: NavBarPage;
  let signInPage: SignInPage;
  let accommodationComponentsPage: AccommodationComponentsPage;
  let accommodationUpdatePage: AccommodationUpdatePage;

  before(async () => {
    await browser.get('/');
    navBarPage = new NavBarPage();
    signInPage = await navBarPage.getSignInPage();
    await signInPage.waitUntilDisplayed();

    await signInPage.username.sendKeys('admin');
    await signInPage.password.sendKeys('admin');
    await signInPage.loginButton.click();
    await signInPage.waitUntilHidden();
    await waitUntilDisplayed(navBarPage.entityMenu);
    await waitUntilDisplayed(navBarPage.adminMenu);
    await waitUntilDisplayed(navBarPage.accountMenu);
  });

  beforeEach(async () => {
    await browser.get('/');
    await waitUntilDisplayed(navBarPage.entityMenu);
    accommodationComponentsPage = new AccommodationComponentsPage();
    accommodationComponentsPage = await accommodationComponentsPage.goToPage(navBarPage);
  });

  it('should load Accommodations', async () => {
    expect(await accommodationComponentsPage.title.getText()).to.match(/Accommodations/);
    expect(await accommodationComponentsPage.createButton.isEnabled()).to.be.true;
  });

  it('should create and delete Accommodations', async () => {
    const beforeRecordsCount = (await isVisible(accommodationComponentsPage.noRecords))
      ? 0
      : await getRecordsCount(accommodationComponentsPage.table);
    accommodationUpdatePage = await accommodationComponentsPage.goToCreateAccommodation();
    await accommodationUpdatePage.enterData();

    expect(await accommodationComponentsPage.createButton.isEnabled()).to.be.true;
    await waitUntilDisplayed(accommodationComponentsPage.table);
    await waitUntilCount(accommodationComponentsPage.records, beforeRecordsCount + 1);
    expect(await accommodationComponentsPage.records.count()).to.eq(beforeRecordsCount + 1);

    await accommodationComponentsPage.deleteAccommodation();
    if (beforeRecordsCount !== 0) {
      await waitUntilCount(accommodationComponentsPage.records, beforeRecordsCount);
      expect(await accommodationComponentsPage.records.count()).to.eq(beforeRecordsCount);
    } else {
      await waitUntilDisplayed(accommodationComponentsPage.noRecords);
    }
  });

  after(async () => {
    await navBarPage.autoSignOut();
  });
});
