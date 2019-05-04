import random, string, Ice
import Bank

PASSWD_LEN = 8

class UserManager():
  def __init__(self):
    self.users = dict()

  def add_user(self, pesel):
    if self.users.__contains__(pesel):
      raise Bank.UserAlreadyExsistsError()
    else:
      password = ''.join(random.choices(string.ascii_uppercase + string.digits, k=PASSWD_LEN))
      self.users[pesel] = password
      return password

  def verify_credentials(self, pesel, password):
    if not self.users.__contains__(pesel):
      raise Bank.UnauthorizedError()
    if self.users[pesel] != password:
      raise Bank.UnauthorizedError()
