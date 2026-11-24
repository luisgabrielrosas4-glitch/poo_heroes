from retos import Reto
from threading import RLock

class Mision:
    LIMITE_ERRORES = 3

    def __init__(self, id_mision: str, reto: Reto, recompensa: int = 10):
        if type(recompensa) is not int or not 0 < recompensa <= 2147483647:
            raise ValueError("La recompensa debe ser positiva")
        self.id_mision = id_mision
        self.reto = reto
        self.estado = "activa"
        self.pistas = []
        self.errores = 0
        self.recompensa = recompensa
        self.recompensa_acreditada = False
        self.partida = None
        self._lock = RLock()

    def procesar_intento(self, respuesta: str) -> bool:
        with self._lock:
            if self.estado != "activa":
                return False
            if self.reto.evaluar_respuesta(respuesta):
                self.completar()
                return True
            self.errores += 1
            if self.errores >= self.LIMITE_ERRORES:
                self.estado = "bloqueada"
            return False

    def consultar_intentos_restantes(self) -> int:
        if self.estado != "activa":
            return 0
        return max(0, self.LIMITE_ERRORES - self.errores)

    def consultar_estado(self) -> str:
        return self.estado

    def completar(self) -> None:
        with self._lock:
            if self.estado == "activa":
                self.estado = "completada"

    def _asignar_partida(self, partida):
        with self._lock:
            if self.partida is not None or partida is None:
                return False
            self.partida = partida
            return True

    def _marcar_recompensa_acreditada(self) -> bool:
        with self._lock:
            if self.estado != "completada" or self.recompensa_acreditada:
                return False
            self.recompensa_acreditada = True
            return True
