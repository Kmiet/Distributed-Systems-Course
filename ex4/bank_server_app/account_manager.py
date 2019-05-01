class AccountManager():
  
  def __init__(self):
    self.accounts = dict()

  def add_account(self, pesel, account, acc_type):
    self.accounts[pesel] = (account, acc_type) 

  def get_account_state(self, pesel):
    return self.accounts[pesel]

  def take_a_loan(self):
    pass