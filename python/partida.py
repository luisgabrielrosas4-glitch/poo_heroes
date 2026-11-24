from typing import List
from mision import Mision
from personajes import Heroe

class Partida:
    def __init__(self):
        self.misiones: List[Mision] = []
        self.recompensas_globales = 0

    def agregar_mision(self, mision: Mision) -> None:
        if mision is None or mision.partida is not None:
            return
        mision.asignar_partida(self)
        self.misiones.append(mision)

    def acreditar_recompensa(self, heroe: Heroe, mision: Mision) -> bool:
        if (heroe is None or mision is None or mision.partida is not self
                or not any(m is mision for m in self.misiones)):
            return False
        if not mision.marcar_recompensa_acreditada():
            return False
        heroe.recibir_recompensa(mision.recompensa)
        self.recompensas_globales += mision.recompensa
        return True

    def iniciar_juego(self) -> None:
        # TODO: Implementar inicio del juego
        pass
