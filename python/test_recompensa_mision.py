import unittest
from concurrent.futures import ThreadPoolExecutor
from mision import Mision
from personajes import Batman
from partida import Partida

class RetoPrueba:
    def evaluar_respuesta(self, respuesta):
        return respuesta == "bien"

class TestRecompensas(unittest.TestCase):
    def test_completar_y_acreditar(self):
        partida, batman = Partida(), Batman()
        m = Mision("1", RetoPrueba(), 30)
        partida.agregar_mision(m)
        self.assertTrue(batman.resolver_mision(m, "bien"))
        self.assertTrue(partida.acreditar_recompensa(batman, m))
        self.assertTrue(m.recompensa_acreditada)
        self.assertEqual(batman.consultar_saldo(), 130)
        self.assertEqual(partida.recompensas_globales, 30)

    def test_sin_repeticiones(self):
        p, b, otro = Partida(), Batman(), Batman()
        m = Mision("2", RetoPrueba(), 40)
        p.agregar_mision(m)
        m.completar()
        self.assertTrue(p.acreditar_recompensa(b, m))
        self.assertFalse(p.acreditar_recompensa(b, m))
        self.assertFalse(p.acreditar_recompensa(otro, m))
        self.assertFalse(b.acreditar_recompensa(m))
        self.assertEqual((b.consultar_saldo(), otro.consultar_saldo(), p.recompensas_globales), (140, 100, 40))

    def test_antes_de_completar(self):
        p, b = Partida(), Batman()
        m = Mision("3", RetoPrueba(), 15)
        p.agregar_mision(m)
        self.assertFalse(p.acreditar_recompensa(b, m))
        self.assertEqual((b.consultar_saldo(), p.recompensas_globales), (100, 0))
        m.completar()
        self.assertTrue(p.acreditar_recompensa(b, m))
        self.assertEqual((b.consultar_saldo(), p.recompensas_globales), (115, 15))

    def test_mision_bloqueada(self):
        p, b = Partida(), Batman()
        m = Mision("4", RetoPrueba(), 25)
        p.agregar_mision(m)
        for _ in range(3):
            m.procesar_intento("mal")
        self.assertEqual(m.consultar_estado(), "bloqueada")
        self.assertFalse(p.acreditar_recompensa(b, m))
        self.assertEqual((b.consultar_saldo(), p.recompensas_globales), (100, 0))

    def test_dos_misiones(self):
        p, b = Partida(), Batman()
        a, c = Mision("5", RetoPrueba(), 20), Mision("6", RetoPrueba(), 50)
        p.agregar_mision(a)
        p.agregar_mision(c)
        a.completar()
        c.completar()
        self.assertTrue(p.acreditar_recompensa(b, a))
        self.assertTrue(p.acreditar_recompensa(b, c))
        self.assertFalse(p.acreditar_recompensa(b, a))
        self.assertEqual((b.consultar_saldo(), p.recompensas_globales), (170, 70))

    def test_mision_ajena(self):
        p, b = Partida(), Batman()
        ajena = Mision("7", RetoPrueba(), 30)
        ajena.completar()
        self.assertFalse(p.acreditar_recompensa(b, ajena))
        self.assertFalse(ajena.recompensa_acreditada)

    def test_directo_antes_de_partida(self):
        partida, batman, otro = Partida(), Batman(), Batman()
        mision = Mision("directa", RetoPrueba(), 30)
        partida.agregar_mision(mision)
        mision.completar()
        self.assertTrue(batman.acreditar_recompensa(mision))
        self.assertEqual((batman.consultar_saldo(), partida.recompensas_globales), (130, 30))
        self.assertFalse(partida.acreditar_recompensa(batman, mision))
        self.assertFalse(batman.acreditar_recompensa(mision))
        self.assertFalse(otro.acreditar_recompensa(mision))
        self.assertFalse(partida.acreditar_recompensa(otro, mision))
        self.assertEqual((batman.consultar_saldo(), otro.consultar_saldo(), partida.recompensas_globales), (130, 100, 30))

    def test_partida_antes_de_directo(self):
        partida, batman = Partida(), Batman()
        mision = Mision("inversa", RetoPrueba(), 30)
        partida.agregar_mision(mision)
        mision.completar()
        self.assertTrue(partida.acreditar_recompensa(batman, mision))
        self.assertFalse(batman.acreditar_recompensa(mision))
        self.assertFalse(partida.acreditar_recompensa(batman, mision))
        self.assertEqual((batman.consultar_saldo(), partida.recompensas_globales), (130, 30))

    def test_cobro_directo_sin_partida(self):
        batman = Batman()
        mision = Mision("sin partida", RetoPrueba(), 30)
        mision.completar()
        self.assertFalse(batman.acreditar_recompensa(mision))
        self.assertEqual(batman.consultar_saldo(), 100)

    def test_no_duplicar_mision_en_otra_partida(self):
        primera, segunda, batman = Partida(), Partida(), Batman()
        mision = Mision("unica", RetoPrueba(), 30)
        primera.agregar_mision(mision)
        segunda.agregar_mision(mision)
        mision.completar()
        self.assertFalse(segunda.acreditar_recompensa(batman, mision))
        self.assertTrue(batman.acreditar_recompensa(mision))
        self.assertEqual((primera.recompensas_globales, segunda.recompensas_globales), (30, 0))

    def test_fallo_en_entrega_no_deja_mision_pagada(self):
        class HeroeConFallo(Batman):
            def _recibir_recompensa(self, cantidad):
                self.saldo += cantidad
                raise RuntimeError("Fallo de prueba")

        partida, heroe = Partida(), HeroeConFallo()
        mision = Mision("fallo", RetoPrueba(), 30)
        partida.agregar_mision(mision)
        mision.completar()
        self.assertFalse(partida.acreditar_recompensa(heroe, mision))
        self.assertEqual((heroe.consultar_saldo(), partida.recompensas_globales), (100, 0))
        self.assertFalse(mision.recompensa_acreditada)
        self.assertTrue(Batman().acreditar_recompensa(mision))
        self.assertEqual(partida.recompensas_globales, 30)

    def test_heroe_incorrecto(self):
        partida = Partida()
        mision = Mision("no heroe", RetoPrueba(), 30)
        partida.agregar_mision(mision)
        mision.completar()
        self.assertFalse(partida.acreditar_recompensa(object(), mision))
        self.assertFalse(mision.recompensa_acreditada)
        self.assertEqual(partida.recompensas_globales, 0)

    def test_saldo_fuera_de_limite(self):
        partida = Partida()
        heroe = Batman()
        heroe.saldo = 2147483647
        mision = Mision("muy grande", RetoPrueba(), 30)
        partida.agregar_mision(mision)
        mision.completar()
        self.assertFalse(partida.acreditar_recompensa(heroe, mision))
        self.assertFalse(mision.recompensa_acreditada)
        self.assertEqual((heroe.saldo, partida.recompensas_globales), (2147483647, 0))
        self.assertTrue(Batman().acreditar_recompensa(mision))
        self.assertEqual(partida.recompensas_globales, 30)

    def test_total_fuera_de_limite(self):
        partida = Partida()
        grande = Mision("grande", RetoPrueba(), 2147483647)
        partida.agregar_mision(grande)
        grande.completar()
        heroe = Batman()
        heroe.saldo = 0
        self.assertTrue(heroe.acreditar_recompensa(grande))
        extra = Mision("extra", RetoPrueba(), 1)
        partida.agregar_mision(extra)
        extra.completar()
        otro = Batman()
        self.assertFalse(partida.acreditar_recompensa(otro, extra))
        self.assertEqual((otro.saldo, partida.recompensas_globales), (100, 2147483647))
        self.assertFalse(extra.recompensa_acreditada)

    def test_cobros_paralelos(self):
        partida, heroe = Partida(), Batman()
        misiones = [Mision(str(i), RetoPrueba(), 3) for i in range(800)]
        for mision in misiones:
            partida.agregar_mision(mision)
            mision.completar()
        def cobrar(par):
            i, mision = par
            return (heroe.acreditar_recompensa(mision) if i % 2 == 0
                    else partida.acreditar_recompensa(heroe, mision))
        with ThreadPoolExecutor(max_workers=8) as pool:
            resultado = list(pool.map(cobrar, enumerate(misiones)))
        self.assertEqual(sum(resultado), 800)
        self.assertEqual(heroe.consultar_saldo(), 2500)
        self.assertEqual(partida.recompensas_globales, 2400)

    def test_varias_partidas_mismo_heroe(self):
        heroe = Batman()
        partidas = [Partida() for _ in range(400)]
        misiones = [Mision(str(i), RetoPrueba(), 2) for i in range(400)]
        for partida, mision in zip(partidas, misiones):
            partida.agregar_mision(mision)
            mision.completar()
        with ThreadPoolExecutor(max_workers=8) as pool:
            resultado = list(pool.map(heroe.acreditar_recompensa, misiones))
        self.assertEqual(sum(resultado), 400)
        self.assertEqual(heroe.consultar_saldo(), 900)
        self.assertEqual(sum(p.recompensas_globales for p in partidas), 800)

    def test_recompensa_invalida(self):
        with self.assertRaises(ValueError):
            Mision("8", RetoPrueba(), 0)
        with self.assertRaises(ValueError):
            Mision("8", RetoPrueba(), -3)
        self.assertEqual(Mision("9", RetoPrueba()).recompensa, 10)
        with self.assertRaises(ValueError):
            Mision("10", RetoPrueba(), 2147483648)

if __name__ == "__main__":
    unittest.main()
