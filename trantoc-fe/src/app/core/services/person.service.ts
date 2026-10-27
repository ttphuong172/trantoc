import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/page-response.model';
import { Person } from '../models/person.model';
import { ParentResponse } from '../models/parent-response.model';
import { ChildResponse } from '../models/child-response.model';
import { CreatePersonRequest } from '../models/create-person-request.model';
import { PersonSummary } from '../models/person-summary.model';
import { GenerationResponse } from '../models/generation-response.model';
import { AncestorNode } from '../models/ancestor-node.model';
import { DescendantNode } from '../models/descendant-node.model';
import { PersonListResponse } from '../models/person-list-response';
import { PersonDetailResponse } from '../models/person-detail-response';

@Injectable({
  providedIn: 'root',
})
export class PersonService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/api/persons';

  getPersons(
    keyword?: string,
    generation?: number,
    page = 0,
    size = 20,
  ): Observable<PageResponse<PersonListResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    if (generation != null) {
      params = params.set('generation', generation);
    }

    return this.http.get<PageResponse<PersonListResponse>>(this.apiUrl, { params });
  }

  getPerson(id: number) {
    return this.http.get<PersonDetailResponse>(`${this.apiUrl}/${id}`);
  }

  getParents(id: number) {
    return this.http.get<ParentResponse>(`${this.apiUrl}/${id}/parents`);
  }

  getChildren(id: number) {
    return this.http.get<ChildResponse[]>(`${this.apiUrl}/${id}/children`);
  }

  createPerson(request: CreatePersonRequest) {
    return this.http.post<Person>(this.apiUrl, request);
  }

  getSpouses(id: number) {
    return this.http.get<PersonSummary[]>(`${this.apiUrl}/${id}/spouses`);
  }

  getAncestorTree(id: number) {
    return this.http.get<AncestorNode>(`${this.apiUrl}/${id}/ancestors`);
  }

  getDescendantTree(id: number) {
    return this.http.get<DescendantNode>(`${this.apiUrl}/${id}/descendants`);
  }

  getCandidateMothers(personId: number) {
    return this.http.get<PersonSummary[]>(`${this.apiUrl}/${personId}/candidate-mothers`);
  }

  updatePerson(id: number, request: CreatePersonRequest) {
    return this.http.put<Person>(`${this.apiUrl}/${id}`, request);
  }

  getGenerations() {
    return this.http.get<number[]>(`${this.apiUrl}/generations`);
  }

  deletePerson(id: number) {
    return this.http.delete<number>(`${this.apiUrl}/${id}`);
  }
}
