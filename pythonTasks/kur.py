import scipy.optimize
from matplotlib import pyplot
import numpy
from scipy import integrate

e_const = 0.497286


def efun():
    res = integrate.quadrature(sub, 0, 1)[0]
    print("sub = %.10f" % res)
    return e_const * res


def sub(t):
    return 1 / numpy.sqrt((t ** 2 + 1) * (3 * t ** 2 + 4))


a_const = 1.213399


def afun():
    return a_const * scipy.optimize.fsolve(lambda x: x ** 2 - numpy.cos(x), numpy.array([0.824]))[0]


def rkf45(f, t, y0, atol):
    r = (integrate.ode(f)
         .set_integrator('dopri5', atol=atol)
         .set_initial_value(y0, t[0]))

    y = numpy.zeros((len(t), len(y0)))
    y[0] = y0

    for it in range(1, len(t)):
        y[it] = r.integrate(t[it])

    if not r.successful():
        raise RuntimeError('Couldn\'t integrate')

    return y[:, 0]


E = efun()


def func(_, u):
    dU = numpy.zeros(u.shape)
    dU[0] = u[1]
    dU[1] = -1.0 * (1.0 + E * (u[0] ** 2)) * u[0]
    return dU


def evaluate(h=0.4, atol=1e-12):
    t0 = 0
    t1 = 16

    A = afun()
    print("A = %.10f\nB = %.10f\nE = %.10f" % (A, B, E))
    # initial conditions
    y0 = numpy.array([A, B])
    t = numpy.arange(t0, t1 + h, h)
    y_rkf45 = rkf45(func, t, y0, atol)
    print("t\tU")
    for i in range(len(t)):
        print("%.1f\t%.10f" % (t[i], y_rkf45[i]))
    pyplot.plot(t, y_rkf45, 'b')
    pyplot.xlabel('t')
    pyplot.ylabel('U(t)')
    pyplot.show()
    return y_rkf45


B = 0

if __name__ == "__main__":
    x = numpy.arange(0.0, 1.0, 0.01)
    y = x ** 2 - numpy.cos(x)
    pyplot.title("Отеделние корней")
    pyplot.grid()
    pyplot.plot(x, y)
    pyplot.show()

    x = numpy.arange(0.8, 0.85, 0.01)
    y = x ** 2 - numpy.cos(x)
    pyplot.title("Отеделние корней")
    pyplot.grid()
    pyplot.plot(x, y)
    pyplot.xticks(x)
    pyplot.show()

    u1 = evaluate()
    u1_ = evaluate(0.4, 1e-7)
    print()
    for i in range(len(u1)):
        print("%.10f" % (u1[i] - u1_[i]))
    print()
    a_const = a_const * 0.999999
    e_const = e_const * 1.000001
    u2 = evaluate()
    print()
    for i in range(len(u1)):
        print("%.10f" % (u1[i] - u2[i]))
    a_const = 1.213399 * 1.00001
    e_const = 0.497286 * 0.99999
    B = 0.00001
    print()
    u3 = evaluate()
    print()
    for i in range(len(u1)):
        print("%.10f" % (u1[i] - u3[i]))
