from abc import ABC, abstractmethod
from nivel_dificultad import NivelDificultad

class Reto(ABC):
    def __init__(self, descripcion: str, dificultad: NivelDificultad):
        self.descripcion = descripcion
        self.dificultad = dificultad
        self._mision = None

    @abstractmethod
    def evaluar_respuesta(self, respuesta: str) -> bool:
        pass

    @abstractmethod
    def generar_problema(self) -> None:
        pass

    def _asociar_mision(self, mision) -> None:
        """Contexto de la misión: Mision se registra aquí desde su constructor.

        Un reto pertenece a una sola misión; asociarlo a otra distinta lanza
        RuntimeError para que una misión cerrada no pueda "liberar" su reto.
        """
        if self._mision is not None and self._mision is not mision:
            raise RuntimeError("El reto ya pertenece a otra misión")
        self._mision = mision

    def admite_modificaciones(self) -> bool:
        """True si el reto no tiene misión (en preparación) o su misión sigue activa.

        El estado se consulta en vivo, sin copiarlo.
        """
        return self._mision is None or self._mision.esta_activa()

    def _exigir_modificable(self) -> None:
        """Las operaciones que modifican el reto la llaman antes de tocar datos."""
        if not self.admite_modificaciones():
            raise RuntimeError("La misión del reto no está activa")

    def aumentar_dificultad(self) -> None:
        self._exigir_modificable()
        if self.dificultad == NivelDificultad.FACIL:
            self.dificultad = NivelDificultad.MEDIO
        elif self.dificultad == NivelDificultad.MEDIO:
            self.dificultad = NivelDificultad.DIFICIL


class RetoSecuencia(Reto):
    def __init__(self, dificultad: NivelDificultad):
        super().__init__("Completar la secuencia numérica", dificultad)
        self.patron_numerico = []

    def evaluar_respuesta(self, respuesta: str) -> bool:
        # TODO: Implementar la validación de la respuesta
        return False

    def generar_problema(self) -> None:
        # TODO: Generar patrón según la dificultad
        pass
