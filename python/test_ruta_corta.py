import unittest
from mision import Mision
from personajes import GreenGoblin, Joker
from reto_ruta_corta import RetoRutaCorta
from retos import RetoSecuencia
from nivel_dificultad import NivelDificultad


class TestRutaCorta(unittest.TestCase):
    def test_ruta_minima(self):
        reto = RetoRutaCorta()
        self.assertEqual(reto.costo_minimo(), 5)
        self.assertTrue(reto.evaluar_respuesta("A-B-C-D"))

    def test_ruta_mas_cara(self):
        self.assertFalse(RetoRutaCorta().evaluar_respuesta("A-B-D"))

    def test_ruta_incorrecta(self):
        reto = RetoRutaCorta()
        self.assertFalse(reto.evaluar_respuesta("A-D"))
        self.assertFalse(reto.evaluar_respuesta("A-X-D"))

    def test_green_goblin(self):
        reto = RetoRutaCorta()
        self.assertTrue(Mision("1", reto).aplicar_dificultad(GreenGoblin()))
        self.assertEqual(reto.costo_minimo(), 6)
        self.assertTrue(reto.evaluar_respuesta("A-B-D"))

    def test_otro_reto(self):
        reto = RetoSecuencia(NivelDificultad.FACIL)
        self.assertFalse(Mision("2", reto).aplicar_dificultad(GreenGoblin()))
        self.assertEqual(reto.dificultad, NivelDificultad.FACIL)

    def test_mision_completada(self):
        reto = RetoRutaCorta()
        mision = Mision("3", reto)
        mision.completar()
        self.assertFalse(mision.aplicar_dificultad(GreenGoblin()))
        self.assertEqual(reto.costo_minimo(), 5)

    def test_mision_bloqueada(self):
        reto = RetoRutaCorta()
        mision = Mision("4", reto)
        for _ in range(3):
            mision.procesar_intento("mal")
        self.assertFalse(mision.aplicar_dificultad(GreenGoblin()))
        self.assertEqual(reto.costo_minimo(), 5)

    def test_no_desconectar(self):
        reto = RetoRutaCorta("A", "B")
        reto.agregar_tramo("A", "B", 3)
        self.assertFalse(Mision("5", reto).aplicar_dificultad(GreenGoblin("A", "B")))
        self.assertTrue(reto.evaluar_respuesta("A-B"))

    def test_crear_mision(self):
        reto = RetoRutaCorta()
        GreenGoblin().plantear_mision("6", reto)
        self.assertTrue(reto.evaluar_respuesta("A-B-D"))


class TestContextoMision(unittest.TestCase):
    def assertIntacto(self, reto, grafo_inicial):
        self.assertEqual(reto.copiar_grafo(), grafo_inicial)
        self.assertEqual(reto.costo_minimo(), 5)
        self.assertTrue(reto.evaluar_respuesta("A-B-C-D"))
        self.assertFalse(reto.evaluar_respuesta("A-B-D"))

    def test_aceptacion_completada_con_obstaculo(self):
        reto = RetoRutaCorta()
        mision = Mision("c1", reto)
        inicial = reto.copiar_grafo()
        self.assertTrue(mision.procesar_intento("A-B-C-D"))
        self.assertEqual(mision.consultar_estado(), "completada")
        self.assertEqual(reto.costo_minimo(), 5)

        goblin = GreenGoblin()
        # Llamada mediante Mision.
        self.assertFalse(mision.aplicar_dificultad(goblin))
        self.assertIntacto(reto, inicial)
        # Acceso directo que antes evitaba la validación.
        goblin.complicar_reto(reto)
        self.assertIntacto(reto, inicial)
        self.assertFalse(goblin.aplicar(reto))
        self.assertIntacto(reto, inicial)
        self.assertFalse(reto.bloquear_tramo("B", "C"))
        self.assertIntacto(reto, inicial)
        self.assertEqual(mision.consultar_estado(), "completada")

    def test_otras_operaciones_rechazadas_en_mision_cerrada(self):
        reto = RetoRutaCorta()
        mision = Mision("c1", reto)
        inicial = reto.copiar_grafo()
        mision.completar()
        with self.assertRaises(RuntimeError):
            reto.agregar_tramo("A", "D", 1)
        with self.assertRaises(RuntimeError):
            reto.generar_problema()
        with self.assertRaises(RuntimeError):
            reto.aumentar_dificultad()
        self.assertEqual(reto.dificultad, NivelDificultad.FACIL)
        self.assertIntacto(reto, inicial)

    def test_mision_bloqueada_directo_y_por_mision(self):
        reto = RetoRutaCorta()
        mision = Mision("c2", reto)
        inicial = reto.copiar_grafo()
        for _ in range(3):
            mision.procesar_intento("mal")
        self.assertEqual(mision.consultar_estado(), "bloqueada")
        goblin = GreenGoblin()
        self.assertFalse(mision.aplicar_dificultad(goblin))
        goblin.complicar_reto(reto)
        self.assertFalse(goblin.aplicar(reto))
        self.assertIntacto(reto, inicial)

    def test_mision_activa_aplica_y_luego_se_congela(self):
        reto = RetoRutaCorta()
        mision = Mision("c3", reto)
        self.assertTrue(GreenGoblin().aplicar(reto))
        self.assertEqual(reto.costo_minimo(), 6)
        mision.completar()
        despues = reto.copiar_grafo()
        self.assertFalse(GreenGoblin("A", "B").aplicar(reto))
        self.assertEqual(reto.copiar_grafo(), despues)
        self.assertEqual(reto.costo_minimo(), 6)

    def test_destino_inaccesible_sigue_rechazado_con_mision_activa(self):
        reto = RetoRutaCorta("A", "B")
        reto.agregar_tramo("A", "B", 3)
        Mision("c4", reto)
        grafo = reto.copiar_grafo()
        self.assertFalse(GreenGoblin("A", "B").aplicar(reto))
        self.assertEqual(reto.copiar_grafo(), grafo)
        self.assertTrue(reto.evaluar_respuesta("A-B"))

    def test_reto_sin_mision_es_modificable_y_una_mision_por_reto(self):
        libre = RetoRutaCorta()
        self.assertTrue(libre.admite_modificaciones())
        self.assertTrue(GreenGoblin().aplicar(libre))

        reto = RetoRutaCorta()
        Mision("c5", reto).completar()
        with self.assertRaises(RuntimeError):
            Mision("c6", reto)

    def test_joker_respeta_estado_de_mision(self):
        reto = RetoSecuencia(NivelDificultad.FACIL)
        Mision("c7", reto).completar()
        Joker().complicar_reto(reto)
        self.assertEqual(reto.dificultad, NivelDificultad.FACIL)


if __name__ == "__main__":
    unittest.main()
