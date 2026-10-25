import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { CreateRelationRequest } from '../models/create-relation-request.model';

@Injectable({
  providedIn: 'root',
})
export class PersonRelationService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/relations';

  create(request: CreateRelationRequest) {
    return this.http.post(this.apiUrl, request);
  }

  changeMother(personId: number, motherId: number) {
    return this.http.put(`${this.apiUrl}/persons/${personId}/mother/${motherId}`, {});
  }

  moveUp(fatherId: number, childId: number) {
    return this.http.post(`${this.apiUrl}/father/${fatherId}/children/${childId}/move-up`, {});
  }

  moveDown(fatherId: number, childId: number) {
    return this.http.post(`${this.apiUrl}/father/${fatherId}/children/${childId}/move-down`, {});
  }
}
