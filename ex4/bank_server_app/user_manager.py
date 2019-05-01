import random, string, Ice
Ice.loadSlice("../slice_definition/Bank.ice")
import Bank

PASSWD_LEN = 8

class UserManager():
  def __init__(self):
    self.users = dict()

  def add_user(self, pesel, data):
    if self.users.__contains__(pesel):
      raise Bank.UserAlreadyExsistsError()
    else:
      password = ''.join(random.choices(string.ascii_uppercase + string.digits, k=PASSWD_LEN))
      data['password'] = password
      self.users[pesel] = user
      return password

  def verify_credentials(self, pesel, password):
    user = self.users.get(pesel)
    if user is None or user.credentials.password !== password:
      raise Bank.UnauthorizedError()
