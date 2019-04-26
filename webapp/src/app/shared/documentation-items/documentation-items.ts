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
const COMPONENTS = 'documentations';
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
    summary: 'Graft Supernodes'
  },
  [WATCHLIST]: {
    name: 'Watchlist',
    summary: 'Graft Supernodes Explorer'
  },
  [COMPONENTS]: {
    name: 'Developer Guides',
    summary: 'Graft Developer Guides'
  },
};


const DOCS: {[key: string]: DocCategory[]} = {
  [COMPONENTS]: [
    {
      id: 'wallet',
      name: 'Wallet',
      summary: 'Wallet Documentations',
      items: [
        {
          id: 'wallet-json-rpc',
          name: 'JSON RPC Methods',
          summary: 'This is a list of the graft-wallet-rpc calls, their inputs and outputs, and examples of each.'
        }
      ]
    },
    {
      id: 'template',
      name: 'Templates',
      summary: 'Templates',
      items: [
        {
          id: 'template',
          name: 'Template',
          summary: 'Template'
        }
      ]
    }
  ],
  [CDK] : [
  ]
};

for (let category of DOCS[COMPONENTS]) {
  for (let doc of category.items) {
    doc.packageName = 'documentations';
  }
}

for (let category of DOCS[CDK]) {
  for (let doc of category.items) {
    doc.packageName = 'cdk';
  }
}

const ALL_COMPONENTS = DOCS[COMPONENTS].reduce(
  (result, category) => result.concat(category.items), []);
const ALL_CDK = DOCS[CDK].reduce((result, cdk) => result.concat(cdk.items), []);
const ALL_DOCS = ALL_COMPONENTS.concat(ALL_CDK);
const ALL_CATEGORIES = DOCS[COMPONENTS].concat(DOCS[CDK]);

@Injectable()
export class DocumentationItems {
  getCategories(section: string): DocCategory[] {
    return DOCS[section];
  }

  getItems(section: string): DocItem[] {
    if (section === COMPONENTS) {
      return ALL_COMPONENTS;
    }
    if (section === CDK) {
      return ALL_CDK;
    }
    return [];
  }

  getItemById(id: string, section: string): DocItem {
    console.log("getItemById " + id + " " + section)
    const sectionLookup = section == 'cdk' ? 'cdk' : 'documentations';
    return ALL_DOCS.find(doc => doc.id === id && doc.packageName == sectionLookup);
  }

  getCategoryById(id: string): DocCategory {
    console.log("getCategoryById " + id )
    return ALL_CATEGORIES.find(c => c.id == id);
  }
}
