from typing import List
from mision import Mision
from personajes import Heroe
from threading import RLock

class Partida:
    def __init__(self):
        self.misiones: List[Mision] = []
        self.recompensas_globales = 0
        self._lock = RLock()

    def agregar_mision(self, mision: Mision) -> None:
        if mision is None:
            return
        with self._lock:
            if mision._asignar_partida(self):
                self.misiones.append(mision)

    def acreditar_recompensa(self, heroe: Heroe, mision: Mision) -> bool:
        if not isinstance(heroe, Heroe) or mision is None:
            return False
        with self._lock:
            if (mision.partida is not self
                    or not any(m is mision for m in self.misiones)):
                return False
            with mision._lock:
                if mision.estado != "completada" or mision.recompensa_acreditada:
                    return False
                nuevo_total = self.recompensas_globales + mision.recompensa
                if nuevo_total > 2147483647:
                    return False
                with heroe._saldo_lock:
                    saldo_anterior = heroe.saldo
                    if (type(saldo_anterior) is not int
                            or saldo_anterior + mision.recompensa > 2147483647):
                        return False
                    try:
                        recibido = heroe._recibir_recompensa(mision.recompensa)
                    except Exception:
                        heroe.saldo = saldo_anterior
                        return False
                    if recibido is not True or heroe.saldo != saldo_anterior + mision.recompensa:
                        heroe.saldo = saldo_anterior
                        return False
                    mision._marcar_recompensa_acreditada()
                    self.recompensas_globales = nuevo_total
                    return True

    def iniciar_juego(self) -> None:
        # TODO: Implementar inicio del juego
        pass
