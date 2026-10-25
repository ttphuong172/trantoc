import { PersonSummary } from './person-summary.model';

export interface ParentResponse {
  father: PersonSummary | null;
  mother: PersonSummary | null;
}
