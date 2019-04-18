import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SupernodeList, SupernodeListModule} from './supernode-list';
import {DocsAppTestingModule} from '../../testing/testing-module';


describe('WatchList', () => {
  let fixture: ComponentFixture<SupernodeList>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [SupernodeListModule, DocsAppTestingModule],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SupernodeList);
  });
});
