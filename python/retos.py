from abc import ABC, abstractmethod
from nivel_dificultad import NivelDificultad

class Reto(ABC):
    def __init__(self, descripcion: str, dificultad: NivelDificultad):
        self.descripcion = descripcion
        self.dificultad = dificultad

    @abstractmethod
    def evaluar_respuesta(self, respuesta: str) -> bool:
        pass

    @abstractmethod
    def generar_problema(self) -> None:
        pass

    def aumentar_dificultad(self) -> None:
        if self.dificultad == NivelDificultad.FACIL:
            self.dificultad = NivelDificultad.MEDIO
        elif self.dificultad == NivelDificultad.MEDIO:
            self.dificultad = NivelDificultad.DIFICIL


class RetoSecuencia(Reto):
    def __init__(self, dificultad: NivelDificultad):
        super().__init__("Completar la secuencia numérica", dificultad)
        self.patron_numerico = []
        self.respuesta_esperada = 0
        self.generar_problema()

    @property
    def dificultad(self) -> NivelDificultad:
        return self._dificultad

    @dificultad.setter
    def dificultad(self, nivel: NivelDificultad) -> None:
        if not isinstance(nivel, NivelDificultad):
            raise ValueError("Nivel de dificultad invalido")
        self._dificultad = nivel
        if hasattr(self, "patron_numerico"):
            self.generar_problema()

    def generar_problema(self) -> None:
        if self.dificultad == NivelDificultad.FACIL:
            self.patron_numerico = [2, 4, 6, 8]
            self.respuesta_esperada = 10
        elif self.dificultad == NivelDificultad.MEDIO:
            self.patron_numerico = [3, 6, 12, 24]
            self.respuesta_esperada = 48
        else:
            self.patron_numerico = [2, 3, 5, 8]
            self.respuesta_esperada = 13

    def aumentar_dificultad(self) -> None:
        super().aumentar_dificultad()

    def copiar(self) -> "RetoSecuencia":
        copia = RetoSecuencia(self.dificultad)
        copia.patron_numerico = self.patron_numerico.copy()
        copia.respuesta_esperada = self.respuesta_esperada
        return copia

    def mostrar_secuencia(self) -> str:
        return ", ".join(str(n) for n in self.patron_numerico) + ", ?"

    def evaluar_respuesta(self, respuesta: str) -> bool:
        if not isinstance(respuesta, str):
            return False
        # Java String.trim() elimina caracteres U+0000..U+0020 en los extremos.
        texto = respuesta.strip("".join(chr(i) for i in range(33)))
        if not texto:
            return False
        numero = 0
        for caracter in texto:
            if not "0" <= caracter <= "9":
                return False
            numero = numero * 10 + ord(caracter) - ord("0")
            if numero > 2147483647:  # Límite de Integer.parseInt en Java.
                return False
        return numero == self.respuesta_esperada
