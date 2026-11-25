import unittest
from retos import RetoSecuencia
from nivel_dificultad import NivelDificultad
from personajes import Joker


class TestRetoSecuencia(unittest.TestCase):
    def comprobar_nivel(self, nivel, secuencia, respuesta):
        reto = RetoSecuencia(nivel)
        self.assertEqual(reto.mostrar_secuencia(), secuencia)
        self.assertTrue(reto.evaluar_respuesta(respuesta))
        self.assertFalse(reto.evaluar_respuesta("999"))
        reto.generar_problema()
        self.assertEqual(reto.mostrar_secuencia(), secuencia)
        self.assertTrue(reto.evaluar_respuesta(respuesta))

    def test_facil(self):
        self.comprobar_nivel(NivelDificultad.FACIL, "2, 4, 6, 8, ?", "10")

    def test_medio(self):
        self.comprobar_nivel(NivelDificultad.MEDIO, "3, 6, 12, 24, ?", "48")

    def test_dificil(self):
        self.comprobar_nivel(NivelDificultad.DIFICIL, "2, 3, 5, 8, ?", "13")

    def test_formato_invalido(self):
        reto = RetoSecuencia(NivelDificultad.FACIL)
        for respuesta in (None, "", "  ", "hola", "10.0", "1 0", "+10", "-10"):
            self.assertFalse(reto.evaluar_respuesta(respuesta))

    def test_cambiar_dificultad(self):
        reto = RetoSecuencia(NivelDificultad.FACIL)
        reto.aumentar_dificultad()
        self.assertEqual(reto.mostrar_secuencia(), "3, 6, 12, 24, ?")
        self.assertTrue(reto.evaluar_respuesta("48"))
        self.assertFalse(reto.evaluar_respuesta("10"))

    def test_joker_facil_a_medio(self):
        reto = RetoSecuencia(NivelDificultad.FACIL)
        mision = Joker().plantear_mision("media", reto)
        self.assertIs(mision.reto, reto)
        self.assertEqual(reto.dificultad, NivelDificultad.MEDIO)
        self.assertEqual(reto.mostrar_secuencia(), "3, 6, 12, 24, ?")
        self.assertTrue(reto.evaluar_respuesta("48"))
        self.assertFalse(reto.evaluar_respuesta("10"))
        self.assertFalse(mision.procesar_intento("10"))
        self.assertEqual(mision.consultar_intentos_restantes(), 2)
        self.assertTrue(mision.procesar_intento("48"))
        self.assertEqual(mision.estado, "completada")

    def test_joker_medio_a_dificil(self):
        reto = RetoSecuencia(NivelDificultad.MEDIO)
        mision = Joker().plantear_mision("dificil", reto)
        self.assertEqual(reto.dificultad, NivelDificultad.DIFICIL)
        self.assertEqual(reto.mostrar_secuencia(), "2, 3, 5, 8, ?")
        self.assertTrue(reto.evaluar_respuesta("13"))
        self.assertFalse(reto.evaluar_respuesta("48"))
        self.assertFalse(mision.procesar_intento("48"))
        self.assertTrue(mision.procesar_intento("13"))
        self.assertEqual(mision.estado, "completada")
        self.assertTrue(reto.evaluar_respuesta("48"))


if __name__ == "__main__":
    unittest.main()
