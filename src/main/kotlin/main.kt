import space.kscience.gdml.*

import java.io.File
import kotlin.io.*

// geometry variables
val worldSize = 1000

val chamber = mapOf<String, Double>(
    "height" to 30.0, "diameter" to 102.0,
    "outerSquareSide" to 134.0, "backplateThickness" to 15.0,
    "teflonWallThickness" to 11.0, "readoutKaptonThickness" to 0.5,
    "readoutCopperThickness" to 0.2, "readoutPlaneSide" to 60.0
)

val geometry = Gdml {
    loadMaterialsFromUrl("https://raw.githubusercontent.com/rest-for-physics/materials/main/NIST.xml")

    val materialsMap = mapOf(
        "Copper" to materials.composite("G4_Cu"),
        "Lead" to materials.composite("G4_Pb"),
    )

    structure {
        val worldMaterial = materials.composite("G4_AIR")
        val worldBox = solids.box(worldSize, worldSize, worldSize, "worldBox")

        val chamberSolidBase =
            solids.box(
                chamber["outerSquareSide"]!!,
                chamber["outerSquareSide"]!!,
                chamber["height"]!!,
                "chamberSolidBase"
            )
        val chamberSolidHole = solids.tube(chamber["diameter"]!! * 0.5, chamber["height"]!!, "chamberSolidHole")
        val chamberSolid = solids.subtraction(chamberSolidBase, chamberSolidHole, "chamberSolid")
        val chamberBackplateSolid = solids.box(
            chamber["outerSquareSide"]!!,
            chamber["outerSquareSide"]!!,
            chamber["backplateThickness"]!!,
            "chamberBackplateSolid"
        )
        val chamberBackplateVolume = volume(materialsMap["Copper"]!!, chamberBackplateSolid, "chamberBackplateVolume")
        val chamberBodyVolume = volume(materialsMap["Copper"]!!, chamberSolid, "chamberBodyVolume")

        val chamberVolume = assembly {
            physVolume(chamberBackplateVolume) {
                name = "chamberBackplate"
                position {
                    z =
                        -chamber["height"]!! * 0.5 - chamber["readoutKaptonThickness"]!! - chamber["backplateThickness"]!! * 0.5
                }
            }
            physVolume(chamberBodyVolume) {
                name = "chamberBody"
            }
        }


        // chamber teflon walls

        // world setup
        world = volume(worldMaterial, worldBox, "world") {

            physVolume(chamberVolume) {
                name = "chamberBody"
                position {
                    y = 200
                }
                rotation {
                    y = 0
                }
            }
        }
    }
}


fun main() {

    val gdmlString = geometry.encodeToString()
    File("Setup.gdml").writeText(gdmlString)
}