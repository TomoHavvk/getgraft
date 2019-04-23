import {Component, NgModule, OnInit} from '@angular/core';
import {GuideItems} from '../../shared/guide-items/guide-items';
import {MatExpansionModule, MatListModule, MatTableDataSource} from '@angular/material';
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
  displayedColumns: string[] = ['BlockchainBasedListTier', 'PublicId', 'Address', 'isOnline', 'StakeAmount', 'StakeExpiringBlock', 'LastUpdateAge', 'ExpirationTime', 'watchlist'];
  database: HttpDatabase | null;
  dataSource = null;
  height = 0;
  data: Node[] = [];
  isLoadingResults = true;
  isMobile: boolean;
  favorite: boolean = false;
  online: boolean = true;
  offline: boolean = true;
  t1: boolean = true;
  t2: boolean = true;
  t3: boolean = true;
  t4: boolean = true;

  filter: string = '';

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(private http: HttpClient, private cookieService: CookieService) {
  }

  changeOption() {
    if (this.favorite) {
      let filtered = this.data.filter(node => node.favorite)
      this.dataSource = new MatTableDataSource(this.withOptions(filtered));
    } else {
      this.dataSource = new MatTableDataSource(this.withOptions(this.data));

    }

    this.dataSource.sort = this.sort;
    this.dataSource.filter = this.filter.trim().toLowerCase();
  }

  onlyFavorite() {
    this.favorite = !this.favorite;
    if (this.favorite) {

      let filtered = this.data.filter(node => node.favorite)
      this.dataSource = new MatTableDataSource(this.withOptions(filtered));
      this.dataSource.sort = this.sort;
      this.dataSource.filter = this.filter.trim().toLowerCase();
    } else {
      this.dataSource = new MatTableDataSource(this.withOptions(this.data));
      this.dataSource.sort = this.sort;
      this.dataSource.filter = this.filter.trim().toLowerCase();
    }

  }

  withOptions(nodes: Node[]): Node[] {
    let online = this.online;
    let offline = this.offline;
    let t1 = this.t1;
    let t2 = this.t2;
    let t3 = this.t3;
    let t4 = this.t4;

    return nodes.filter(node =>
      ((node.isOnline && online) || (!node.isOnline && offline)) &&
      ((node.BlockchainBasedListTier == "1" && t1) || (node.BlockchainBasedListTier == "2" && t2) || (node.BlockchainBasedListTier == "3" && t3) || (node.BlockchainBasedListTier == "4" && t4))
    );
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
      this.dataSource = new MatTableDataSource(this.withOptions(data));
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

  hideAddress(address: string): string {
    return address.substring(0, 5) +  "..."
      + address.substring(address.length - 5, address.length)
  }

  copyToClipboard(address: string) {
    const el = document.createElement('textarea');
    el.value = address;
    document.body.appendChild(el);
    el.select();
    document.execCommand('copy');
    document.body.removeChild(el);
  };
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
  imports: [MatListModule, MatCardModule, MatExpansionModule, FormsModule, ReactiveFormsModule, MatSelectModule, MatCheckboxModule, MatGridListModule, MatInputModule, MatFormFieldModule, MatSortModule, MatProgressSpinnerModule, MatTableModule, MatPaginatorModule, RouterModule, FooterModule, CommonModule],
  exports: [SupernodeList],
  declarations: [SupernodeList],
  providers: [GuideItems, ComponentPageTitle],
})
export class SupernodeListModule {
}
