import {Component, NgModule, OnInit} from '@angular/core';
import {GuideItems} from '../../shared/guide-items/guide-items';
import {MatListModule, MatTableDataSource} from '@angular/material';
import {MatFormFieldModule} from '@angular/material';
import {MatInputModule} from '@angular/material';
import {MatSelectModule} from '@angular/material';
import {MatCardModule} from '@angular/material';
import {MatCheckboxModule} from '@angular/material';
import {RouterModule} from '@angular/router';
import {MatGridListModule} from '@angular/material';
import {FooterModule} from '../../shared/footer/footer';
import {CommonModule} from '@angular/common';
import {ComponentPageTitle} from '../page-title/page-title';
import {CookieService} from 'ngx-cookie';

import {HttpClient} from '@angular/common/http';
import {ViewChild, AfterViewInit, OnDestroy} from '@angular/core';
import {
  MatPaginator,
  MatProgressSpinnerModule,
  MatSort,
  MatSortModule,
  MatTableModule,
  MatPaginatorModule
} from '@angular/material';
import {interval, merge, Observable, of as observableOf, Subscription} from 'rxjs';
import {catchError, map, startWith, switchMap} from 'rxjs/operators';
import {ReactiveFormsModule, FormsModule} from '@angular/forms';

@Component({
  selector: 'app-supernode',
  templateUrl: './supernode-list.html',
  styleUrls: ['./supernode-list.scss']
})


export class SupernodeList implements AfterViewInit, OnDestroy, OnInit {
  private subscription: Subscription;
  displayedColumns: string[] = ['BlockchainBasedListTier', 'PublicId', 'isOnline', 'StakeAmount', 'StakeExpiringBlock', 'LastUpdateAge', 'ExpirationTime', 'watchlist'];
  database: HttpDatabase | null;
  dataSource = null;
  height = 0;
  data: Node[] = [];
  isLoadingResults = true;
  isMobile: boolean;
  filter: string = '';

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(private http: HttpClient, private cookieService: CookieService) {
  }

  ngOnInit() {
    this.isMobile = /Android|iPhone/i.test(window.navigator.userAgent)
    console.log("isMobile - " + this.isMobile)
  }

  ngAfterViewInit() {
    this.isLoadingResults = true;
    this.loadData();
    merge(this.sort.sortChange).subscribe(data => {
      this.dataSource.sort = this.sort;
    });
    this.autoRefresh()
  }

  ngOnDestroy() {
    this.subscription.unsubscribe()
  }

  loadData() {
    this.database = new HttpDatabase(this.http);
    merge()
      .pipe(
        startWith({}),
        switchMap(() => {

          return this.database!.getRepoIssues(
            this.sort.active, this.sort.direction);
        }),
        map(data => {
          this.isLoadingResults = false;
          this.height = data.height
          return data.nodes;
        }),
        catchError(() => {
          this.isLoadingResults = false;
          return observableOf([]);
        })
      ).subscribe(data => {

      this.data = data;

      if (this.cookieService.get('watchlist')) {

        let watchlist: string [] = JSON.parse(this.cookieService.get('watchlist'));

        this.data.forEach(function (node) {
          if (watchlist.indexOf(node.PublicId) != -1) {
            node.favorite = true;
          }
        })
      }
      this.dataSource = new MatTableDataSource(data);
      this.dataSource.sort = this.sort;
      this.dataSource.filter = this.filter.trim().toLowerCase();
    });
  }

  applyFilter() {
    console.log(this.filter);
    this.dataSource.filter = this.filter.trim().toLowerCase();
  }

  addToWatchlist(node: Node) {
    if (this.cookieService.get('watchlist')) {
      let watchlist: string [] = JSON.parse(this.cookieService.get('watchlist'));
      watchlist.push(node.PublicId);
      this.cookieService.put('watchlist', JSON.stringify(watchlist));
      node.favorite = true
    } else {
      let watchlist: string [] = [node.PublicId];
      this.cookieService.put('watchlist', JSON.stringify(watchlist));
      node.favorite = true
    }
  }

  removeFromWatchlist(node: Node) {
    let watchlist: string [] = JSON.parse(this.cookieService.get('watchlist'));
    watchlist = watchlist.filter(x => x !== node.PublicId);
    node.favorite = false;
    this.cookieService.put('watchlist', JSON.stringify(watchlist))
  }

  autoRefresh() {
    const source = interval(120000);
    this.subscription = source.subscribe(val => {
      this.loadData()
    });
  }
}

export interface Supernodes {
  nodes: Node[];
  info: Info;
  height: number;
}

export interface Animal {
  name: string;
  sound: string;
}

export interface Node {
  Address: string;
  PublicId: string;
  StakeAmount: string;
  StakeFirstValidBlock: string;
  StakeExpiringBlock: string;
  IsStakeValid: string;
  BlockchainBasedListTier: string;
  AuthSampleBlockchainBasedListTier: string;
  IsAvailableForAuthSample: string;
  LastUpdateAge: string;
  ExpirationTime: string;
  isOnline: string;
  favorite: boolean;
}

export interface Info {
  nodesOnline: string;
  totalStake: string;
  t1: string;
  t2: string;
  t3: string;
  t4: string;
}


export class HttpDatabase {
  constructor(private http: HttpClient) {
  }

  getRepoIssues(sort: string, order: string): Observable<Supernodes> {
    const href = 'http://coinonist.com/sn';

    return this.http.get<Supernodes>(href);
  }
}

@NgModule({
  imports: [MatListModule, MatCardModule, FormsModule, ReactiveFormsModule, MatSelectModule, MatCheckboxModule, MatGridListModule, MatInputModule, MatFormFieldModule, MatSortModule, MatProgressSpinnerModule, MatTableModule, MatPaginatorModule, RouterModule, FooterModule, CommonModule],
  exports: [SupernodeList],
  declarations: [SupernodeList],
  providers: [GuideItems, ComponentPageTitle],
})
export class SupernodeListModule {
}
