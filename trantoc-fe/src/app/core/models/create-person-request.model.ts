export interface CreatePersonRequest {
  fullName: string;

  aliasName?: string;

  birthYear?: number;
  deathYear?: number;

  gender: string;

  rootAncestor: boolean;

  address?: string;

  burialPlace?: string;

  deathAnniversary?: string;

  notes?: string;
}
