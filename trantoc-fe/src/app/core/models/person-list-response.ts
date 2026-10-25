export interface PersonListResponse {
  id: number;
  fullName: string;
  aliasName?: string;
  generation: number;
  fatherName?: string;
  fatherAliasName?: string;

  motherName?: string;
  motherAliasName?: string;

  spouseName?: string;
  spouseAliasName?: string;
  role: string;

  children?:string[];
}
