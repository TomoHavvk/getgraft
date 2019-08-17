import {Injectable} from '@angular/core';

export interface DocItem {
  id: string;
  name: string;
  summary?: string;
  packageName?: string;
  examples?: string[];
}

export interface DocCategory {
  id: string;
  name: string;
  items: DocItem[];
  summary?: string;
}

export interface DocSection {
  name: string;
  summary: string;
}

const CDK = 'cdk';
const WATCHLIST = 'watchlist';
const SUPERNODES = 'supernodes';
const GUIDES = 'guides';
export const SECTIONS: {[key: string]: DocSection} = {
  // [CDK]: {
  //   name: 'CDK',
  //   summary: 'The Component Dev Kit (CDK) is a set of tools that implement common interaction ' +
  //   'patterns whilst being unopinionated about their presentation. It represents an abstraction ' +
  //   'of the core functionalities found in the Angular Material library, without any styling ' +
  //   'specific to Material Design. Think of the CDK as a blank state of well-tested functionality ' +
  //   'upon which you can develop your own bespoke components.'
  // },
  [SUPERNODES]: {
    name: 'Supernodes',
    summary: 'Graft Supernodes Explorer'
  },
  [WATCHLIST]: {
    name: 'Watchlist',
    summary: 'Graft Supernodes Watchlist'
  },
  [GUIDES]: {
    name: 'Guides',
    summary: 'Graft Guides'
  },
};


const DOCS: {[key: string]: DocCategory[]} = {
  [GUIDES]: [
    {
      id: 'api',
      name: 'API',
      summary: 'Site API',
      items: [
        {
          id: 'supernode-api',
          name: 'Supernode',
          summary: 'Graft Supernode - API'
        }
      ]
    },
    {
      id: 'general',
      name: 'General',
      summary: 'Graft ecosystem',
      items: [
        {
          id: 'become-part-ecosystem',
          name: 'How to become a part of Graft ecosystem',
          summary: 'How to become a part of Graft ecosystem'
        }
      ]
    },
    {
      id: 'wallet',
      name: 'Wallet',
      summary: 'Wallet guides',
      items: [
        {
          id: 'wallet-json-rpc',
          name: 'JSON RPC Methods',
          summary: 'This is a list of the graft-wallet-rpc calls, their inputs and outputs, and examples of each.'
        }
      ]
    },
    {
      id: 'supernode',
      name: 'Supernode',
      summary: 'Supernode guides',
      items: [
        {
          id: 'rta-supernode-setup',
          name: 'RTA Supernode',
          summary: 'Graft RTA Supernode - step-by-step setup instructions'
        },
        {
          id: 'proxy-supernode-setup',
          name: 'Proxy Supernode',
          summary: 'Graft Proxy Supernode - step-by-step setup instructions'
        }
      ]
    },
    {
      id: 'service-broker',
      name: 'Service Broker',
      summary: 'Exchange Broker guides',
      items: [
        {
          id: 'exchange-broker-setup',
          name: 'Exchange Broker',
          summary: 'Graft Exchange Broker - step-by-step setup instructions'
        },
        {
          id: 'pay-in-broker-setup',
          name: 'Pay-in Broker',
          summary: 'Graft Pay-in Broker - step-by-step setup instructions'
        },
        {
          id: 'payout-broker-setup',
          name: 'Payout Broker',
          summary: 'Graft Payout Broker - step-by-step setup instructions'
        }
      ]
    },
    {
      id: 'mining',
      name: 'Mining',
      summary: 'Mining guides',
      items: [
        {
          id: 'mining-setup',
          name: 'How to become a miner',
          summary: 'How to become a miner - Simple step-by-step Graft mining setup instructions'
        }
      ]
    }
  ],
  [CDK] : [
  ]
};

for (let category of DOCS[GUIDES]) {
  for (let doc of category.items) {
    doc.packageName = 'guides';
  }
}

for (let category of DOCS[CDK]) {
  for (let doc of category.items) {
    doc.packageName = 'cdk';
  }
}

const ALL_COMPONENTS = DOCS[GUIDES].reduce(
  (result, category) => result.concat(category.items), []);
const ALL_CDK = DOCS[CDK].reduce((result, cdk) => result.concat(cdk.items), []);
const ALL_DOCS = ALL_COMPONENTS.concat(ALL_CDK);
const ALL_CATEGORIES = DOCS[GUIDES].concat(DOCS[CDK]);

@Injectable()
export class DocumentationItems {
  getCategories(section: string): DocCategory[] {
    return DOCS[section];
  }

  getItems(section: string): DocItem[] {
    if (section === GUIDES) {
      return ALL_COMPONENTS;
    }
    if (section === CDK) {
      return ALL_CDK;
    }
    return [];
  }

  getItemById(id: string, section: string): DocItem {
    const sectionLookup = section == 'cdk' ? 'cdk' : 'guides';
    return ALL_DOCS.find(doc => doc.id === id && doc.packageName == sectionLookup);
  }

  getCategoryById(id: string): DocCategory {
    return ALL_CATEGORIES.find(c => c.id == id);
  }
}
