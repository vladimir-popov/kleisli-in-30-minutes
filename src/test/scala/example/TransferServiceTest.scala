package example

import java.util.concurrent.ConcurrentHashMap

import cats.Id
import cats.implicits._
import org.scalatest.FunSuite

import scala.collection.mutable
import scala.concurrent.{ Await, Future }
import scala.util.Try
import scala.concurrent.duration._

class TransferServiceTest extends FunSuite {

  /**
    * Пример выполнения `transfer` синхронно и без транзакции.
    */
  test("without transaction") {
    // given:
    val accFrom = Account(1, 100)
    val accTo = Account(2, 50)
    val value = 20
    val mockRepo = new AccountRepo[Id] {
      val updateInvocations = new mutable.Queue[Account]()
      override def getById(id: Long) = if (id == 1) accFrom else accTo

      override def update(acc: Account) = {
        updateInvocations.enqueue(acc); true
      }
    }
    val service = new TransferService(mockRepo)

    // when:
    val result: Boolean = service.transfer(accFrom.id, accTo.id, value)

    // then:
    assert(mockRepo.updateInvocations.dequeue() == Account(1, 80))
    assert(mockRepo.updateInvocations.dequeue() == Account(2, 70))
  }

  /**
   * Пример выполнения `transfer` синхронно в транзакции.
   */
  test("with transaction") {
    // given:
    type Tx = mutable.Map[Long, Account]
    val accFrom = Account(1, 100)
    val accTo = Account(2, 50)
    val value = 20
    val persistence = mutable.Map[Long, Account](1L → accFrom, 2L → accTo)
    val mockRepo = new AccountRepoTx[Try, Tx] {
      override def getById(id: Long, tx: Tx) = Try {
        tx(id)
      }
      override def update(acc: Account, tx: Tx) = Try {
        tx.update(acc.id, acc)
        true
      }
    }
    val txManager = new TransactionManager[Try, Tx] {
      override def createTx(): Try[Tx] = Try {
        mutable.Map[Long, Account]()
      }
      override def begin(transaction: Tx): Try[Unit] = Try {
        transaction ++= persistence
      }
      override def commit(transaction: Tx): Try[Unit] = Try {
        persistence ++= transaction
      }
      override def rollback(transaction: Tx): Try[Unit] = Try {
        transaction.clear()
      }
    }
    val service = new TransferService(new AccountRepoKleisli(mockRepo))

    // when:
    val result: Try[Boolean] = txManager.withTx(
      service.transfer(accFrom.id, accTo.id, value).run
    )
    // then:
    assert(persistence(1L) == Account(1, 80))
    assert(persistence(2L) == Account(2, 70))
  }

  /**
   * Пример выполнения `transfer` асинхронно.
   */
  test("asynchronous") {
    import scala.concurrent.ExecutionContext.Implicits.global

    // given:
    val accFrom = Account(1, 100)
    val accTo = Account(2, 50)
    val value = 20
    val mockRepo = new AccountRepo[Future] {
      val updateInvocations = new ConcurrentHashMap[Long, Account]()
      override def getById(id: Long) = Future {
        if (id == 1) accFrom else accTo
      }

      override def update(acc: Account) = Future {
        updateInvocations.put(acc.id, acc); true
      }
    }
    val service = new TransferService(mockRepo)

    // when:
    val result: Boolean = Await.result(service.transfer(accFrom.id, accTo.id, value), 5.seconds)

    // then:
    assert(mockRepo.updateInvocations.get(1L) == Account(1, 80))
    assert(mockRepo.updateInvocations.get(2L) == Account(2, 70))
  }
}
