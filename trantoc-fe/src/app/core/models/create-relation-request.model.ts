export interface CreateRelationRequest {
  personId: number;
  relatedPersonId: number;
  relationType: string;
}
