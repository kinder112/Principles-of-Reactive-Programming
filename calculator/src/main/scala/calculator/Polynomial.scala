package calculator

import Math._

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double],
      c: Signal[Double]): Signal[Double] = {
    new Signal(b() * b() - 4*a()*c())
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
      c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = {
    new Signal({ 
          if(delta() < 0) Set()
          else if(delta() == 0) Set( (-b()) / (2*a()) )
          else Set( (( (-b()+ sqrt(delta())) / (2*a()) )), (( (-b() - sqrt(delta())) / (2*a()) )) )
        })
  }
}
