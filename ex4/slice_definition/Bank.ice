module Bank {
  
  exception InvalidCredentials {};
  exception LoanRejectionError {
    string reason;
  };
  exception RegistrationError {
    string reason;
  };
  exception UnauthorizedError {};
  exception UserAlreadyExistsError {};

  enum AccountType {
    PREMIUM,
    STANDARD
  };

  enum Currency {
    AUD,
    CHF,
    EUR,
    GBP,
    USD,
    PLN
  };

  struct LoanOffer {
    string currency;
    double amountInPLN;
    double amount;
  };

  struct AccountInfo {
    string firstName;
    string lastName;
    double amount;
    AccountType type;
  };

  struct UserCredentials {
    string pesel; // user identifier
    string password;
  };

  struct RegistrationResponse {
    string password;
    AccountType accountType;
    Account* account;
  };

  interface Bank {
    RegistrationResponse registerNewAccount(string firstName, string lastName, string pesel, int monthlyDeposit) throws RegistrationError, UserAlreadyExistsError;
    Account* login(UserCredentials credentials) throws UnauthorizedError;
  };

  interface Account {
    AccountInfo getCurrentState(UserCredentials credentials) throws UnauthorizedError;
  };
  
  interface PremiumAccount extends Account {
    LoanOffer takeALoan(UserCredentials credentials, enum currency, double amount) throws UnauthorizedError, LoanRejectionError;
  };
};