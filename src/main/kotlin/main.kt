import space.kscience.gdml.Gdml
import space.kscience.gdml.encodeToString

import java.io.File
import kotlin.io.*

// geometry variables
val worldSize = 1000

val chamber = mapOf(
    "height" to 30, "diameter" to 102,
    "outerSquareSide" to 134, "backplateThickness" to 1,
)

val geometry = Gdml {
    structure {
        val worldMaterial = materials.composite("G4_AIR")
        val worldBox = solids.box(worldSize, worldSize, worldSize, "worldBox")

        val copperMaterial = materials.composite("G4_Cu")
        val chamberSolidBase =
            solids.box(
                chamber["outerSquareSide"]!!,
                chamber["outerSquareSide"]!!,
                chamber["height"]!!,
                "chamberSolidBase"
            )
        val chamberSolidHole = solids.tube(chamber["diameter"]!! / 2, chamber["height"]!!, "chamberSolidHole")
        val chamberSolid = solids.subtraction(chamberSolidBase, chamberSolidHole, "chamberSolid")
        val chamberBodyVolume = volume(copperMaterial, chamberSolid, "chamberBodyVolume")
        val chamberBackplateSolid = solids.box(
            chamber["outerSquareSide"]!!,
            chamber["outerSquareSide"]!!,
            chamber["backplateThickness"]!!,
            "chamberBackplateSolid"
        )
        val chamberBackplateVolume = volume(copperMaterial, chamberBackplateSolid, "chamberBackplateVolume")
        // chamber teflon walls

        // world setup
        world = volume(worldMaterial, worldBox, "world") {

            physVolume(chamberBodyVolume) {
                name = "chamberBody"
            }
        }
    }
}

fun main() {

    val gdmlString = geometry.encodeToString()
    File("Setup.gdml").writeText(gdmlString)
}