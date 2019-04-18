import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {WatchList, WatchListModule} from './watch-list';
import {DocsAppTestingModule} from '../../testing/testing-module';


describe('WatchList', () => {
  let fixture: ComponentFixture<WatchList>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [WatchListModule, DocsAppTestingModule],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WatchList);
  });
});
