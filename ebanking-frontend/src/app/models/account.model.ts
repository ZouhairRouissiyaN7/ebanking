export type AccountStatus = 'CREATED' | 'ACTIVATED' | 'SUSPENDED';

export type AccountType = 'CURRENT' | 'SAVING';

export type OperationType = 'DEBIT' | 'CREDIT';

export interface BankAccount {
  id: string;
  customerId: number;
  balance: number;
  status: AccountStatus;
  createdAt: string;
  type: AccountType;
  overdraft: number | null;
  interestRate: number | null;
}

export interface AccountOperation {
  id: number;
  type: OperationType;
  amount: number;
  balanceAfter: number;
  operationDate: string;
  accountId: string;
  transferReference: string | null;
}

export interface AccountHistory {
  content: AccountOperation[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface MoneyRequest {
  amount: number;
  description?: string;
}

export interface DebitRequest extends MoneyRequest {
  accountId: string;
}

export interface CreditRequest extends MoneyRequest {
  accountId: string;
}

export interface TransferRequest {
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  description?: string;
}

export interface TransferResult {
  transferReference: string;
  sourceAccountId: string;
  destinationAccountId: string;
  amount: number;
  sourceBalance: number;
  destinationBalance: number;
}

export interface CreateCurrentAccountRequest {
  customerId: number;
  overdraft: number;
}

export interface CreateSavingAccountRequest {
  customerId: number;
  interestRate: number;
}
