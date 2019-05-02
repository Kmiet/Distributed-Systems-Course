module Bank {
  
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

  struct Date {
		int year;
		byte month;
		byte day;
	};

  struct Loan {
    string currency;
    double amountTaken;
    double amountReturned;
    double interestRate;
    Date takenOn;
    Date dueTo;
  };

  sequence<Loan> Loans;

  struct LoanAmount {
    double plnAmount; // amount to return in PLN
    double foreignCurrencyAmount; // amount to return in foreign currency
  };

  struct AccountState {
    double value;
    Loans loans;
  };

  struct UserCredentials {
    string pesel; // user identifier
    string password;
  };

  struct UserAccount {
    UserCredentials credentials;
    string firstName;
    string lastName;
    AccountType accountType;
  };

  struct BankAccount {
    UserAccount user;
    AccountState state;
  };

  struct RegistrationResponse {
    string password;
    AccountType accountType;
  };

  interface User {
    RegistrationResponse registerNewAccount(string firstName, string lastName, string pesel, int monthlyDeposit) throws RegistrationError, UserAlreadyExistsError;
  };

  interface Account {
    AccountState getCurrentState(UserCredentials credentials) throws UnauthorizedError;
    LoanAmount takeALoan(UserCredentials credentials, string currency, double amount, Date returnTime) throws UnauthorizedError, LoanRejectionError;
  };

  interface Client extends Account, User {};
};