package ru.dokwork.example.sync

import org.scalatest.FunSuite
import ru.dokwork.example.Account

import scala.collection.mutable

class TransferServiceTest extends FunSuite {

  test("without transaction") {
    // given:
    val accountFrom = Account(1, 100)
    val accountTo = Account(2, 50)
    val value = 20
    val mockRepo = new AccountRepo {
      // сюда будем записывать аргументы метода update
      val updateInvocations = mutable.Queue[Account]()
      override def getById(id: Long) = {
        if (id == 1) accountFrom else accountTo
      }
      override def update(acc: Account) = {
        updateInvocations.enqueue(acc)
        true
      }
    }
    val service = new TransferService(mockRepo)

    // when:
    service.transfer(accountFrom.id, accountTo.id, value)

    // then:
    assert(mockRepo.updateInvocations.dequeue() == Account(1, 80))
    assert(mockRepo.updateInvocations.dequeue() == Account(2, 70))
  }
}
