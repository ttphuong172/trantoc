import { Routes } from '@angular/router';
import { Home } from './home/home';
import { PersonList } from './features/person/person-list/person-list';
import { PersonDetail } from './features/person/person-detail/person-detail';
import { AddChild } from './features/person/add-child/add-child';
import { AddSpouse } from './features/person/add-spouse/add-spouse';
import { PersonEdit } from './features/person/person-edit/person-edit';
import { ChangeMother } from './features/person/change-mother/change-mother';
import { FamilyTree } from './features/family-tree/family-tree';

export const routes: Routes = [
  {
    path: '',
    component: PersonList,
  },
  {
    path: 'persons/:id',
    component: PersonDetail,
  },
  {
    path: 'persons/:id/add-child',
    component: AddChild,
  },

  {
    path: 'persons/:id/add-spouse',
    component: AddSpouse,
  },

  {
    path: 'persons/:id/edit',
    component: PersonEdit,
  },

  {
    path: 'persons/:id/change-mother',
    component: ChangeMother,
  },

  {
    path: 'family-tree',
    component: FamilyTree,
  },
];
