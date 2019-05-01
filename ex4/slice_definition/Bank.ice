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
    PLN,
    USD
  };

  struct DateTime {
		int year;
		byte month;
		byte day;
		byte dour;
		byte minute;
		byte second;
	};

  struct Loan {
    Currency currency;
    double amountTaken;
    double amountReturned;
    double interestRate;
    DateTime takenOn;
    DateTime dueTo;
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
    long pesel; // user identifier
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

  struct RegisrationResponse {
    string password;
    AccountType accountType;
  };

  interface User {
    RegisrationResponse registerNewAccount(string firstName, string lastName, long pesel, int monthlyDeposit) throws RegistrationError, UserAlreadyExistsError;
  };

  interface Account {
    AccountState getCurrentState(UserCredentials credentials) throws UnauthorizedError;
    LoanAmount takeALoan(UserCredentials credentials, Currency currency, double amount, DateTime returnTime) throws UnauthorizedError, LoanRejectionError;
  };
};